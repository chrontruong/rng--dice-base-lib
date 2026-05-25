package com.io.begstd.dice.rtp.repository;

import com.io.begstd.dice.rtp.model.RtpPlaySessionStore;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
@Slf4j
public class MyFileRepositoryImpl implements MyFileRepository {
    private final String HEADER_RTP_OBJECT = "Number Times, WinGreaterBet, WinRate, FreeWinRate, BonusWinRate, CountDrawTicket, "
            + "Total Win, Total Bet, Total Normal Win, Number FreeSpin, Total Free Win, Total Jackpot Win, Total Bonus Win, "
            + "RTP, RTP Normal, RTP Free, RTP Bonus, "
            + "Trigger Normal2Free, Trigger Normal2Bonus,Trigger Free2Free, Trigger Free2Bonus, "
            + "Count Grand, Count Major, Count Minor, Count Mini,"
            
            + "RTP Jackpot, RTP JP Grand, RTP JP Major, RTP JP Minor, RTP JP Mini, "
            + "RTP remaining, RTP Grand Remaining, RTP Major Remaining, RTP Minor Remaining, RTP Mini Remaining, "
            
            + "Total Normal Big Win, Total Free Big Win, Total Bonus For Normal Big Win, Total Bunus For Free Big Win,"
            + "Count Time Normal Big Win,Count Time Free Big Win,Count Time Bonus For Normal Big Win,Count Time Bonus For Free Big Win, Created Date, "

            + "totalAmountBigWin 1 < win < 5,"
            + "totalAmountBigWin 5  <= win < 10,"
            + "totalAmountBigWin 10 <= win < 15,"
            + "totalAmountBigWin 15 <= win < 20,"
            + "totalAmountBigWin 20 <= win < 25,"
            + "totalAmountBigWin 25 <= win < 30,"
            + "totalAmountBigWin 30 <= win < 35,"
            + "totalAmountBigWin 35 <= win < 40,"
            + "totalAmountBigWin 40 <= win < 45,"
            + "totalAmountBigWin 45 <= win < 50,"
            + "totalAmountBigWin 50 <= win,"
            + "numberBigWin 1 < win < 5,"
            + "numberBigWin 5  <= win < 10,"
            + "numberBigWin 10 <= win < 15,"
            + "numberBigWin 15 <= win < 20,"
            + "numberBigWin 20 <= win < 25,"
            + "numberBigWin 25 <= win < 30,"
            + "numberBigWin 30 <= win < 35,"
            + "numberBigWin 35 <= win < 40,"
            + "numberBigWin 40 <= win < 45,"
            + "numberBigWin 45 <= win < 50,"
            + "numberBigWin 50 <= win";
    
    

    public MyFileRepositoryImpl() {
    }
    
    @Override
    public boolean writeDataReportPlaySession(List<RtpPlaySessionStore> stores, String fileName, Date created) {
        
        for(RtpPlaySessionStore playSessionResult: stores) {
            writeDataToFile(fileName, buildData(playSessionResult, created));
        }
        log.info("PlaySession is stored to database");
        return true;
    }
    
    
    private void writeDataToFile(String fileName, String data) {
        FileWriter fileWriter;
        PrintWriter printWriter = null;
        try {
            fileWriter = new FileWriter(fileName, true);
            printWriter = new PrintWriter(fileWriter);
            if (data != null) {
                printWriter.println(data);
            }
            
        } catch (IOException e) {
            log.error(e.getMessage() , e);
        } finally {
            
            if(printWriter != null) {
                printWriter.close();
            }
            if(printWriter != null) {
                printWriter.close();
            }
        }
    }
    
    private void writeDataDetailToFile(String fileName, String data) {
        FileWriter fileWriter;
        PrintWriter printWriter = null;
        try {
            fileWriter = new FileWriter(fileName, true);
            printWriter = new PrintWriter(fileWriter);
            printWriter.println(data);
            printWriter.println("-------------------------------------------------------------------------------------------");
            
        } catch (IOException e) {
            log.error(e.getMessage() , e);
        } finally {
            
            if(printWriter != null) {
                printWriter.close();
            }
            if(printWriter != null) {
                printWriter.close();
            }
        }
    }
    
    private String buildData(RtpPlaySessionStore playSessionResult, Date created) {
        String pattern = "yyyy-MM-dd-HH-mm-ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        NumberFormat formatter;
        NumberFormat formatterRtp;
        formatter = new DecimalFormat("###.##");
        formatterRtp = new DecimalFormat("###.#######");
        
        double rtp = (playSessionResult.getTotalWin()/playSessionResult.getTotalBet())*100.000;
        double rtpNormal = (playSessionResult.getTotalNormalWin()/playSessionResult.getTotalBet())*100.000;
        double rtpFreeSpin = (playSessionResult.getTotalFreeWin()/playSessionResult.getTotalBet())*100.000;
        
        double rtpJackpot = (playSessionResult.getTotalJackPotWin()/playSessionResult.getTotalBet())*100.000;
        double rtpMini = (playSessionResult.getTotalJackPotMiniWin() / playSessionResult.getTotalBet()) * 100.000;
        double rtpMinor = (playSessionResult.getTotalJackPotMinorWin() / playSessionResult.getTotalBet()) * 100.000;
        double rtpMajor = (playSessionResult.getTotalJackPotMajorWin() / playSessionResult.getTotalBet()) * 100.000;
        double rtpGrand = (playSessionResult.getTotalJackPotGrandWin() / playSessionResult.getTotalBet()) * 100.000;
        
        double rtpBonus = (playSessionResult.getTotalMiniWin()/playSessionResult.getTotalBet())*100.000;
        
        StringBuffer result = new StringBuffer();
        result.append(playSessionResult.getNumberSpin());
        result.append(",");
        result.append(playSessionResult.getCountWinGreaterBet());
        result.append(",");
        result.append((float)playSessionResult.getCountWinGreaterBet()/playSessionResult.getNumberSpin());
        result.append(",");
        result.append(formatter.format(playSessionResult.getFreeWinRate()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getBonusWinRate()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getCountDrawTicket()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getTotalWin()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getTotalBet()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getTotalNormalWin()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getNumberSpinFree()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getTotalFreeWin()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getTotalJackPotWin()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getTotalMiniWin()));
        result.append(",");
        result.append(formatterRtp.format(rtp));
        result.append(",");
        result.append(formatterRtp.format(rtpNormal));
        result.append(",");
        result.append(formatterRtp.format(rtpFreeSpin));
        result.append(",");
        result.append(formatterRtp.format(rtpBonus));
        
        result.append(",");
        result.append(formatterRtp.format(playSessionResult.getNumberCountNormalToFree()));
        result.append(",");
        result.append(formatterRtp.format(playSessionResult.getNumberCountNormalToBonus()));
        result.append(",");
        result.append(formatterRtp.format(playSessionResult.getNumberCountFreeToFree()));
        result.append(",");
        result.append(formatterRtp.format(playSessionResult.getNumberCountFreeToBonus()));
        result.append(",");
        result.append(formatterRtp.format(playSessionResult.getNumberCountGrand()));
        result.append(",");
        result.append(formatterRtp.format(playSessionResult.getNumberCountMajor()));
        result.append(",");
        result.append(formatterRtp.format(playSessionResult.getNumberCountMinor()));
        result.append(",");
        result.append(formatterRtp.format(playSessionResult.getNumberCountMini()));
        result.append(",");
        result.append(formatterRtp.format(rtpJackpot));
        result.append(",");
        result.append(formatterRtp.format(rtpGrand));
        result.append(",");
        result.append(formatterRtp.format(rtpMajor));
        result.append(",");
        result.append(formatterRtp.format(rtpMinor));
        result.append(",");
        result.append(formatterRtp.format(rtpMini));
       
        result.append(",");
        result.append(formatterRtp.format(playSessionResult.getTotalRTPRemaining()));
        result.append(",");
        result.append(formatterRtp.format(playSessionResult.getTotalRTPGrandRemaining()));
        result.append(",");
        result.append(formatterRtp.format(playSessionResult.getTotalRTPMajorRemaining()));
        result.append(",");
        result.append(formatterRtp.format(playSessionResult.getTotalRTPMinorRemaining()));
        result.append(",");
        result.append(formatterRtp.format(playSessionResult.getTotalRTPMiniRemaining()));

        result.append(",");
        result.append(formatter.format(playSessionResult.getTotalNormalBigWin()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getTotalFreeBigWin()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getTotalBonusForNormalBigWin()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getTotalBonusForFreeBigWin()));
        result.append(",");
        result.append(playSessionResult.getNumberNormalBigWin());
        result.append(",");
        result.append(playSessionResult.getNumberFreeBigWin());
        result.append(",");
        result.append(playSessionResult.getNumberBonusForNormalBigWin());
        result.append(",");
        result.append(playSessionResult.getNumberBonusForFreeBigWin());
        result.append(",");
        result.append(simpleDateFormat.format(created));

        result.append(",");
        result.append(formatter.format(playSessionResult.getTotalBigWinFromGT1To5()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getTotalBigWinFrom5To10()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getTotalBigWinFrom10To15()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getTotalBigWinFrom15To20()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getTotalBigWinFrom20To25()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getTotalBigWinFrom25To30()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getTotalBigWinFrom30To35()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getTotalBigWinFrom35To40()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getTotalBigWinFrom40To45()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getTotalBigWinFrom45To50()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getTotalBigWinFrom50Upto()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getNumberBigWinFromGT1To5()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getNumberBigWinFrom5To10()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getNumberBigWinFrom10To15()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getNumberBigWinFrom15To20()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getNumberBigWinFrom20To25()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getNumberBigWinFrom25To30()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getNumberBigWinFrom30To35()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getNumberBigWinFrom35To40()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getNumberBigWinFrom40To45()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getNumberBigWinFrom45To50()));
        result.append(",");
        result.append(formatter.format(playSessionResult.getNumberBigWinFrom50Upto()));
        
        return result.toString();
    }


    @Override
    public boolean writeHeaderReportPlaySession(String fileName) {
        writeDataToFile(fileName, HEADER_RTP_OBJECT);
        return false;
    }

    @Override
    public boolean writeDetailReportPlaySession(String fileName) {
        writeDataToFile(fileName, null);
        return false;
    }

    @Override
    public boolean writeDataDetailReportPlaySession(List<RtpPlaySessionStore> stores, String fileName, Date created) {
        for(RtpPlaySessionStore playSessionResult: stores) {
            writeDataDetailToFile(fileName, playSessionResult.getMessage());
        }
        log.info("PlaySession is stored to database");
        return true;
    }
}
