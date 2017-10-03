package org.neo.gomina.model.svn;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.api.projects.CommitLogEntry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SvnRepository {

    private final static Logger logger = LogManager.getLogger(SvnRepository.class);

    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public SvnRepository() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public SvnDetails getSvnDetails(String projectId) {
        try {
            List<SvnDetails> svnData = mapper.readValue(new File("data/projects.svn.yaml"), new TypeReference<List<SvnDetails>>() {});
            for (SvnDetails svnDatum : svnData) {
                if (StringUtils.equals(svnDatum.id, projectId)) {
                    return svnDatum;
                }
            }
        }
        catch (IOException e) {
            logger.error("Error retrieving SVN data for " + projectId, e);
        }
        return null;
    }

    public List<CommitLogEntry> getCommitLog(String projectId) {

        try {
            List<Map<String, Object>> svnData = mapper.readValue(new File("data/projects.svn.yaml"), new TypeReference<List<Map<String, Object>>>() {});
            for (Map<String, Object> svnDatum : svnData) {
                if (StringUtils.equals((String)svnDatum.get("id"), projectId)) {
                    return buildFrom((List<Map<String, Object>>)svnDatum.get("log"));
                }
            }
        }
        catch (IOException e) {
            logger.error("Error retrieving SVN data for " + projectId, e);
        }
        return new ArrayList<>();
    }

    private List<CommitLogEntry> buildFrom(List<Map<String, Object>> list) {
        List<CommitLogEntry> result = new ArrayList<>();
        if (list != null) {
            for (Map<String, Object> stringObjectMap : list) {
                result.add(buildFrom(stringObjectMap));
            }
        }
        return result;
    }

    private CommitLogEntry buildFrom(Map<String, Object> map) {
        CommitLogEntry entry = new CommitLogEntry();
        entry.revision = (String)map.get("revision");
        entry.date = (Date)map.get("date");
        entry.author = (String)map.get("author");
        entry.message = (String)map.get("message");
        return entry;
    }

}
