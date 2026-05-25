package com.io.begstd.dice.rtp.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.io.begstd.dice.factory.GameRuleFactory;
import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.model.config.DiceConfigMode;
import com.io.begstd.dice.model.gamerule.DenominationLevel;
import com.io.begstd.dice.rtp.allinone.RtpAllInOneService;
import com.io.begstd.dice.rtp.config.RtpProperties;
import com.io.begstd.dice.rtp.model.RtpArgs;
import com.io.begstd.dice.rtp.model.RtpPlaySessionStore;
import com.io.begstd.dice.rtp.model.RtpResult;
import com.io.begstd.dice.rtp.repository.MyFileRepository;
import com.io.begstd.dice.rtp.service.RtpService;
import com.io.begstd.dice.rtp.task.RtpScenario;
import com.io.begstd.dice.rtp.util.TaskUtil;
import com.io.begstd.dice.rtp.util.UserUtil;
import com.io.begstd.dice.utils.ConfigManager;
import com.io.begstd.dice.annotation.Scenario;
import com.io.begstd.dice.command.ExtraBetLevelCmd;
import com.io.begstd.dice.exception.DiceGameException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
public class RtpExecutor {
    public static final String RTPSTART = "===RTPSTART=======";
    public static final String RTPEND = "===RTPEND=======";
    private static int NUMBER_STORE_DATA = 100;
    private static int BATCH_RUN_RTP_CHECK = 50000;

    @Autowired
    private RtpProperties rtpProperties;

    @Autowired
    private RtpScenario rtpScenario;

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private RtpService rtpService;

    @Autowired
    private MyFileRepository myFileRepository;

    @Autowired
    private ConfigManager configManager;

    @Autowired
    @Qualifier("objectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private GameRuleFactory gameRuleFactory;

    @Autowired
    private RtpAllInOneService rtpAllInOneService;

    @Autowired
    private Environment env;

    @Scenario
    public RtpResult execute(RtpArgs rtpArgs, RtpResult rtpResult) {
        String rtpSelected = parseInputRtp(rtpArgs);

        NumberFormat formatter = new DecimalFormat("#,###,###,###,###.##");
        String pattern = "yyyy-MM-dd-HH-mm-ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String path = null;
        try {
            path = getJarContainingFolder(RtpExecutor.class);
        } catch (Exception e) {

            log.error(e.getMessage() , e);
        }
        if (StringUtils.isEmpty(rtpArgs.getUuid())) {
            rtpArgs.setUuid(UUID.randomUUID().toString());
        }

        //apply betLines in rtp
        List<Integer> lineIds = new ArrayList<>();
        if (rtpArgs.getBetLines() != null && !rtpArgs.getBetLines().equals("")) {
            if (!rtpArgs.getBetLines().equals("all")) {
                List<String> stringList = Arrays.asList(rtpArgs.getBetLines().split(","));
                lineIds = stringList.stream().map(Integer::valueOf).collect(Collectors.toList());
            }
        }

        rtpScenario.setTotalBet(rtpArgs.getTotalBet(), rtpArgs.getCurrency(), lineIds);
        rtpScenario.setFreeSpinOption(rtpArgs.getFreeSpinOption());

        int numberUsers = rtpArgs.getTotalUser();
        long startValue = 0;
        long endValue = rtpArgs.getTotalSpin();
        boolean isLastRunTime = false;
        if (rtpArgs.getTotalSpin() > BATCH_RUN_RTP_CHECK) {
            endValue = BATCH_RUN_RTP_CHECK;
        } else {
            isLastRunTime = true;
        }
        if (endValue < numberUsers) {
            rtpArgs.setTotalUser((int) endValue);
            numberUsers = rtpArgs.getTotalUser();
        }
        IDiceMachineConfig config = (IDiceMachineConfig) configManager.getConfigMain(DiceConfigMode.NORMAL);
        String serviceId = config.serviceId();

//        SpinCmd spinCmd = new SpinCmd().totalBetId(rtpArgs.getTotalBet()).lineIds(lineIds);
//        DenominationLevel demonLevel = spinCmd.toBetPerLineModel(slotMachineConfigForNormal, null);
        String[] userList = UserUtil.generateUsers(rtpArgs.getTotalUser(), rtpProperties.getUserPrefix());

        Date created = new Date();

        String fileName = serviceId + "_" + rtpArgs.getUuid() + "_" + simpleDateFormat.format(created) + ".csv";
        String fileNameDetail = serviceId + "_" + rtpArgs.getUuid() + "_" + simpleDateFormat.format(created) + ".txt";
        if (!StringUtils.isEmpty(path)) {
            fileName = path + File.separator + fileName;
            fileNameDetail = path + File.separator + fileNameDetail;
            log.error("Write to file in : " + fileName);
            log.error("Write to detail file in : " + fileNameDetail);
        }
        if (rtpArgs.getNeedSteps() > 0) {
            myFileRepository.writeHeaderReportPlaySession(fileName);
            myFileRepository.writeDetailReportPlaySession(fileNameDetail);
        }
        List<RtpPlaySessionStore> stores = new ArrayList<RtpPlaySessionStore>();
        long numberRunStepValue = endValue - startValue;

        long beginTime = System.currentTimeMillis();
        log.error("Starting Rtp for {} users, total times ={} with betId={} and FreeSpinOption={} betLines {} spins at {}, rtp {}",
                rtpArgs.getTotalUser(), formatter.format(rtpArgs.getTotalSpin()), rtpArgs.getTotalBet(), rtpArgs.getFreeSpinOption(),
                rtpArgs.getBetLines(), beginTime, rtpSelected);
        if (rtpProperties.isNeedJoinGame()) {
            joinGame(userList);
        }
        while (startValue < rtpArgs.getTotalSpin() && endValue <= rtpArgs.getTotalSpin()) {
            List<Callable<RtpResult>> rtpTasks = createRtpTasks(rtpArgs, userList, numberRunStepValue, numberUsers);
            try {
                //log.error("startValue ="+ startValue +"  -- endValue="+endValue +" numberRunStepValue=" +numberRunStepValue + "
                // --numberUsers="+numberUsers);
                List<Future<RtpResult>> results = ((ThreadPoolTaskExecutor) taskExecutor).getThreadPoolExecutor().invokeAll(rtpTasks);
                //log.error("End call this patch!!!!");
                calculateRtpResult(rtpResult, results);
//                rtpResult.setBetAmount(demonLevel.amount().value().doubleValue());
//                rtpResult.setBetId(rtpArgs.getTotalBet());
                RtpPlaySessionStore display = rtpResult.display(rtpArgs.getUuid(), rtpArgs.getModeDisplay(), isLastRunTime, endValue,
                        serviceId);
                Map<String, Object> allInOneData = rtpResult.buildAllInOneData(rtpAllInOneService.rtpAllInOneConfig(), display);
                if (rtpArgs.getNeedSteps() > 0) {
                    stores.add(display);
                    if (rtpAllInOneService.getNumberTimes().contains((int) display.getNumberSpin())) {
                        rtpAllInOneService.addStore(allInOneData);
                    }
                    if (stores.size() >= NUMBER_STORE_DATA) {
                        myFileRepository.writeDataReportPlaySession(stores, fileName, created);
                        myFileRepository.writeDataDetailReportPlaySession(stores, fileNameDetail, created);
                        stores.clear();
                    }
                }
                if (isLastRunTime) {
                    if (!rtpAllInOneService.getNumberTimes().contains((int) display.getNumberSpin())) {
                        rtpAllInOneService.addStore(allInOneData);
                    }
                    log.error(display.getMessage());
                }

                startValue += BATCH_RUN_RTP_CHECK;
                endValue += BATCH_RUN_RTP_CHECK;
                if (endValue >= rtpArgs.getTotalSpin()) {
                    isLastRunTime = true;
                    numberRunStepValue = rtpArgs.getTotalSpin() - startValue;
                    endValue = rtpArgs.getTotalSpin();
                    if (numberRunStepValue < numberUsers) {
                        rtpArgs.setTotalUser((int) numberRunStepValue);
                        numberUsers = rtpArgs.getTotalUser();
                    }
                }
            } catch (Exception e) {
                log.error("calculateRtpResult ERROR" , e);
            }
        }
        if (rtpArgs.getNeedSteps() > 0 && stores.size() > 0) {
            myFileRepository.writeDataReportPlaySession(stores, fileName, created);
            myFileRepository.writeDataDetailReportPlaySession(stores, fileNameDetail, created);
        }

        if (!Arrays.asList(env.getActiveProfiles()).contains("test")) {
            String allInOneFile = serviceId + "_AllInOne_" + rtpArgs.getUuid() + "_" + simpleDateFormat.format(created) + ".xlsx";
            rtpAllInOneService.writeReport(allInOneFile, path);
        }

        log.error("Total time {} milliseconds", System.currentTimeMillis() - beginTime);
        if (rtpProperties.isNeedJoinGame()) {
            leaveGame(userList);
        }
        return rtpResult;
    }

    private Map<String, String> getJsonResult(String rtpResult) {
        Map<String, String> jsonResult = new HashMap<>();
        try {
            if (rtpResult.contains("\n")) {
                String[] split = rtpResult.split("\n");
                for (String line : split) {
                    if (line.contains(":")) {
                        String[] keyValue = line.split(":");
                        if (keyValue.length == 2) {
                            jsonResult.put(keyValue[0].trim(), keyValue[1].trim());
                        }
                    }
                }
            }
        } catch (Exception e) {
            jsonResult.put("result", rtpResult);
        }
        return jsonResult;
    }


    private List<Callable<RtpResult>> createRtpTasks(RtpArgs rtpArgs, String[] userList, long spinForEachStep, int numberUser) {
        List<Callable<RtpResult>> rtpTasks = new ArrayList<>();
        long minimumNumberOfSpinForEachUser = spinForEachStep / numberUser;
        long remainingSpinNeedToDistribute = spinForEachStep % numberUser;
        for (int i = 0; i < numberUser; i++) {
            String userName = userList[i];
            if (remainingSpinNeedToDistribute > 0) {
                rtpTasks.add(TaskUtil.convertRtpTaskToCallableTask(userName, rtpScenario, minimumNumberOfSpinForEachUser + 1));
            } else {
                rtpTasks.add(TaskUtil.convertRtpTaskToCallableTask(userName, rtpScenario, minimumNumberOfSpinForEachUser));
            }
            remainingSpinNeedToDistribute--;
        }

        return rtpTasks;
    }

    private void joinGame(String[] userList) {
        for (String userId : userList) {
            try {
                rtpService.joinGame(userId, rtpProperties.getCommandId(), null);
            } catch (DiceGameException e) {
                log.error("joinGame ERROR :", e);
            }
        }
    }


    private void calculateRtpResult(RtpResult rtpResult, List<Future<RtpResult>> answers) {
        answers.forEach(rtpResultFuture-> {

            try {
                RtpResult rtpResultFutureValue = rtpResultFuture.get();
                rtpResult.merge(rtpResultFutureValue);
            } catch (InterruptedException | ExecutionException e) {
                log.error(e.getMessage() , e);
            }
        });
    }

    private void leaveGame(String[] userList) {
        for (String userId : userList) {
            rtpService.leaveGame(userId, rtpProperties.getCommandId());
        }
    }

    private String getJarContainingFolder(Class aclass) throws Exception {
        CodeSource codeSource = aclass.getProtectionDomain().getCodeSource();

        File jarFile = null;

        if (codeSource.getLocation() != null) {
            try {
                jarFile = new File(codeSource.getLocation().toURI());
            } catch (Exception e) {

            }
        }

        if (jarFile == null) {
            String path = aclass.getResource(aclass.getSimpleName() + ".class").getPath();
            String jarFilePath = path.substring(path.indexOf(":") + 1, path.indexOf("!"));
            jarFilePath = URLDecoder.decode(jarFilePath, "UTF-8");
            jarFile = new File(jarFilePath);
        }
        return jarFile.getParentFile().getAbsolutePath();
    }


    public boolean isValidBet(String betId, String currency) {
        IDiceMachineConfig normalConfig = (IDiceMachineConfig) configManager.getConfigMain(DiceConfigMode.NORMAL);
        List<ExtraBetLevelCmd> extraBetLevels = normalConfig.extraBetLevels();
        List<DenominationLevel> denominationLevels = normalConfig.getDenominationLevelsForBet(currency);
        if (CollectionUtils.isEmpty(extraBetLevels)) {
            for (DenominationLevel cmd : denominationLevels) {
                if (!CollectionUtils.isEmpty(cmd.env()) && cmd.id().equals(betId)) {
                    return true;
                }
            }
        } else {
            for (DenominationLevel cmd : denominationLevels) {
                if (!CollectionUtils.isEmpty(cmd.env())) {
                    for (ExtraBetLevelCmd exCmd : extraBetLevels) {
                        String betIdSystem = "" + cmd.id().charAt(0) + exCmd.id();
                        if (betId.equals(betIdSystem)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private String parseInputRtp(RtpArgs args) {
        if (args.getUuid() != null && args.getUuid().contains("_")) {
            String[] tmp = args.getUuid().split("_");
            return tmp[0];
        }
        return null;
    }

}
