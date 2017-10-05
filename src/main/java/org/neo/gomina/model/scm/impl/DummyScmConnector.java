package org.neo.gomina.model.scm.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.scm.ScmConnector;
import org.neo.gomina.model.scm.model.Commit;
import org.neo.gomina.model.scm.model.ScmDetails;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DummyScmConnector implements ScmConnector {

    private final static Logger logger = LogManager.getLogger(DummyScmConnector.class);

    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public DummyScmConnector() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public ScmDetails getSvnDetails(String svnUrl) {
        try {
            List<ScmDetails> svnData = mapper.readValue(new File("data/projects.svn.yaml"), new TypeReference<List<ScmDetails>>() {});
            for (ScmDetails svnDatum : svnData) {
                if (StringUtils.equals(svnDatum.url, svnUrl)) {
                    return svnDatum;
                }
            }
        }
        catch (IOException e) {
            logger.error("Error retrieving SVN data for " + svnUrl, e);
        }
        return null;
    }

    @Override
    public List<Commit> getCommitLog(String svnUrl) {

        try {
            List<Map<String, Object>> svnData = mapper.readValue(new File("data/projects.svn.yaml"), new TypeReference<List<Map<String, Object>>>() {});
            for (Map<String, Object> svnDatum : svnData) {
                if (StringUtils.equals((String)svnDatum.get("url"), svnUrl)) {
                    return buildFrom((List<Map<String, Object>>)svnDatum.get("log"));
                }
            }
        }
        catch (IOException e) {
            logger.error("Error retrieving SVN data for " + svnUrl, e);
        }
        return new ArrayList<>();
    }

    private List<Commit> buildFrom(List<Map<String, Object>> list) {
        List<Commit> result = new ArrayList<>();
        if (list != null) {
            for (Map<String, Object> stringObjectMap : list) {
                result.add(buildFrom(stringObjectMap));
            }
        }
        return result;
    }

    private Commit buildFrom(Map<String, Object> map) {
        Commit entry = new Commit();
        entry.revision = (String)map.get("revision");
        entry.date = (Date)map.get("date");
        entry.author = (String)map.get("author");
        entry.message = (String)map.get("message");
        return entry;
    }

}
