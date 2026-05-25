package com.io.begstd.dice.rtp.allinone;

import com.io.begstd.dice.rtp.model.RtpPlaySessionStore;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

@Slf4j
public class RtpAllInOneServiceImpl implements RtpAllInOneService {

    @Autowired
    @Qualifier("rtpAllInOneConfig")
    private Map<String, ?> rtpAllInOneConfig;

    private final List<RtpPlaySessionStore> stores = new ArrayList<RtpPlaySessionStore>();
    private final Queue<Map<String, Object>> dataStorages = new LinkedList<>();
    private boolean isFirstExport = true;
    private int colNo;
    private int topHeaderNo;

    @Override
    public Map<String, ?> rtpAllInOneConfig() {
        return rtpAllInOneConfig;
    }

    @Override
    public List<Integer> getNumberTimes() {
        try {
            return ((List<Number>) this.rtpAllInOneConfig.get("numberTimes"))
                    .stream()
                    .map(Number::intValue)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void addStore(RtpPlaySessionStore store) {
        stores.add(store);
    }

    @Override
    public void addStore(Map<String, Object> storage) {
        dataStorages.add(storage);
    }

    @Override
    public void writeReport(String fileName, String path) {
        String allInOnePath = path + File.separator + fileName;

        XSSFWorkbook workbook = new XSSFWorkbook();
        if (isFirstExport) {
            XSSFSheet sheet = workbook.createSheet("AllInOne");
            List<String> topHeader = (List<String>) rtpAllInOneConfig.get("topHeader");
            List<String> topSubHeader = (List<String>) rtpAllInOneConfig.get("topSubHeader");

            Row row0 = sheet.createRow(0);
            writeRowData(topHeader.toArray(String[]::new), row0);
            Row row1 = sheet.createRow(1);
            writeRowData(topSubHeader.toArray(String[]::new), row1);
            colNo = topHeader.size() - 1;
            topHeaderNo = topHeader.size() - 1;
            isFirstExport = false;
        }

        XSSFSheet sheet = workbook.getSheet("AllInOne");
        List<String> fieldExport = (List<String>) rtpAllInOneConfig.get("fieldExport");

        while (!dataStorages.isEmpty()) {
            Map<String, Object> data = dataStorages.poll();
            colNo += 1;
            int rowIndex = 2;
            sheet.autoSizeColumn(colNo);
            Cell topHeaderCell = sheet.getRow(0).createCell(colNo);
            topHeaderCell.setCellValue(String.valueOf(data.get("numberSpin")));

            for (String fieldName : fieldExport) {
                Object value = data.getOrDefault(fieldName, "");
                if (colNo - topHeaderNo == 1) {
                    Row row = sheet.createRow(rowIndex);
                    Cell cell = row.createCell(0);
                    cell.setCellValue(fieldName);
                }
                Row row = sheet.getRow(rowIndex);
                Cell cell = row.createCell(colNo);
                cell.setCellValue(String.valueOf(value));
                rowIndex++;
            }
        }
        exportFile(allInOnePath, workbook);
    }

    public void exportFile(String fileName, Workbook workbook) {
        try {
            FileOutputStream fileOut = new FileOutputStream(fileName);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void writeRowData(String[] data, Row row) {
        for (int i = 0; i < data.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(data[i]);
        }
    }

}
