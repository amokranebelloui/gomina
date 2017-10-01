package org.neo.gomina.model.svn;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SvnRepository {

    private final static Logger logger = LogManager.getLogger(SvnRepository.class);

    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

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

}
