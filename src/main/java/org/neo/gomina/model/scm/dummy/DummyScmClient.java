package org.neo.gomina.model.scm.dummy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.scm.ScmClient;
import org.neo.gomina.model.scm.Commit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DummyScmClient implements ScmClient {

    private final static Logger logger = LogManager.getLogger(DummyScmClient.class);

    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public DummyScmClient() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public List<Commit> getLog(String url, String rev, int count) throws Exception {
        try {
            Map<String, Object> projectData = getProjectData(url);
            List<Map<String, Object>> log = (List<Map<String, Object>>) projectData.get("log");
            return buildFrom(log);
        }
        catch (Exception e) {
            logger.error("Error retrieving SVN data for " + url, e);
        }
        return new ArrayList<>();
    }

    @Override
    public String getFile(String url, String rev) throws IOException {
        Map<String, Object> projectData = getProjectData(url.replace("/trunk/pom.xml", ""));
        List<Map<String, Object>> log = (List<Map<String, Object>>) projectData.get("log");
        Map<String, Object> commit = StringUtils.equals(rev, "-1")
                ? log !=null && log.size()>0 ? log.get(0) : null
                : findRevision(log, rev);
        return commit != null ? sampleFile((String)commit.get("version")) : null;
    }

    private String sampleFile(String version) {
        return "<project><version>" + version + "</version></project>";
    }

    private Map<String, Object> findRevision(List<Map<String, Object>> log, String rev) {
        for (Map<String, Object> commit : log) {
            if (StringUtils.equals((String)commit.get("revision"), rev)) {
                return commit;
            }
        }
        return null;
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

    private Map<String, Object> getProjectData(String url) throws java.io.IOException {
        List<Map<String, Object>> svnData = mapper.readValue(new File("data/projects.svn.yaml"), new TypeReference<List<Map<String, Object>>>() {});
        Map<String, Object> projectData = null;
        for (Map<String, Object> svnDatum : svnData) {
            if (StringUtils.equals((String)svnDatum.get("url"), url)) {
                projectData = svnDatum;
                break;
            }
        }
        return projectData;
    }

}
