package com.io.begstd.dice.rtp.repository;

import com.io.begstd.dice.rtp.model.RtpPlaySessionStore;

import java.util.Date;
import java.util.List;

public interface MyFileRepository {
    public boolean writeDataReportPlaySession(List<RtpPlaySessionStore> stores, String fileName, Date created);
    public boolean writeHeaderReportPlaySession(String fileName);
    // for detail
    public boolean writeDetailReportPlaySession(String fileName);
    public boolean writeDataDetailReportPlaySession(List<RtpPlaySessionStore> stores, String fileName, Date created);
    
}
