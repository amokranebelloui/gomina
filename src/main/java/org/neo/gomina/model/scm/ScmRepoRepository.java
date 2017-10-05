package org.neo.gomina.model.scm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo.gomina.model.scm.impl.DummyScmClient;
import org.neo.gomina.model.scm.impl.TmateSoftSvnClient;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScmRepoRepository {

    private final static Logger logger = LogManager.getLogger(ScmRepoRepository.class);

    Map<String, ScmClient> clients = new HashMap<>();

    public ScmRepoRepository() throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<Map<String, String>> repos = mapper.readValue(
                new File("data/repos.yaml"),
                new TypeReference<List<Map<String, String>>>() {}
        );

        for (Map<String, String> repo : repos) {
            String id = repo.get("id");
            String type = repo.get("type");
            String location = repo.get("location");

            try {
                clients.put(id, buildScmClient(type, location));
            }
            catch (Exception e) {
                logger.info("Cannot build SCM client for " + id, e);
            }
        }
    }

    private ScmClient buildScmClient(String type, String location) throws Exception {
        switch (type) {
            case "svn": return new TmateSoftSvnClient(location);
            case "dummy": return new DummyScmClient();
        }
        return null;
    }

    public ScmClient get(String id) {
        return clients.get(id);
    }

}
