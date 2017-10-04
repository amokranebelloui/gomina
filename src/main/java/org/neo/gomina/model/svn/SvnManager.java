package org.neo.gomina.model.svn;

public class SvnManager {

    /*
    private List<String> projectIds = new ArrayList<>();
    private Map<String, SvnProject> projects = new HashMap<>();

    public void initFromProjectsMap(Map<String, SvnProject> map) {
        this.projectIds = new ArrayList(map.keySet());
        this.projects = map;
    }


    public Map<String, SvnProject> getProjectsMap() {
        return projects;
    }

    public SvnProject get(String projectName) {
        SvnProject svnProject = projects.get(projectName);
        if (svnProject == null) {
            // FIXME Manage case when no project is linked
            //throw new RuntimeException("Project " + projectName + " not defined");
            svnProject = new SvnProject();
            svnProject.setChanges(new ArrayList<SvnLogItem>());
        }
        return svnProject;
    }


    public Collection<SvnProject> getProjects() {
        List<SvnProject> result = new ArrayList<>();
        for (String projectId : projectIds) {
            result.add(projects.get(projectId));
        }
        return result;
        //return projects.values();
    }

    public Collection<SvnProject> getUnreleaseProjects() {
        List<SvnProject> result = new ArrayList<>();
        for (String projectId : projectIds) {
            SvnProject project = projects.get(projectId);
            if (project.getChangeCount() > 0) {
                result.add(project);
            }
        }
        return result;
    }

    private void update(String projectName, SvnProject project) {
        if (!this.projectIds.contains(projectName)) {
            this.projectIds.add(projectName);
        }
        this.projects.put(projectName, project);
    }
    */
}
