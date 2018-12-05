Model: Component, System, Env, Instance, Dependency, Event

## Components
- List of registered components  
- Local configuration
  - ~~components.json file~~
  - Per component .yaml file, (folder, database?)
- Plugin System for extensions
  - Basic information
    - Description
    - Ownership: **Commit activity a good indicator** 
    - Importance/Risk 
  - Documentation
    - Formats: Markdown
    - Locations: In the component's SCM 
  - SCM: 
    - Commit log 
    - Versions
    - **Branches management**
  - Sonar: LineOfCode, Coverage
  - Jenkins: 
    - Build status
    - **Which version/revision has been built** 
    - **Branches management**
  - **Dependencies**
    - Service registry entry names
    - to other components
    - Along with code    
- Ability view information stored along with code 
  at any time in the past

## Environments
- Reference component (duplicate data, just link)
- Inventory: (env > services > instances)
- Monitoring and real time status
- SSH: where/what installed, unwanted stuff
  - Simple folder structure
  - **Other models, like schedulers: Docker/Kubernetes**
- Cluster status, **Zookeeper Integration**

- Do the right subscriptions when a new env/instance is added

## Inventory
List and manage all your components, environments, instances
- Components: owners, SCM url, description, code metrics (coverage, loc, etc)
- Envs and instances: what should run and where
- Alert on unexpected instances, and instances not running where they should
- 
 
## Monitoring
Visualize the environments, and the status of the deployment

- Find out the current config of a component
- Whats handles by the component if you have some sharding/partitioning

## Best Practices
- For each deployed/running instance you should be able to say      
  on which env it belongs, it's type, the service, and the instance id (sharding/replication).
  you should also be able to tell which version is deployed, running (eventually the SCM revision tag)
  
  
  
## Indicators

### General Information
- PID
- Version
- Last start, start duration
- Component specific statuses, like connectivity OK, circuit breaker status, etc.  

### Moving Data
- Communication flow between components: frequency, size, usefulness 
- Number of threads

### Events
- Releases
- Configuration Changes
- Restarts
- Leader/ Primary switches
- Maintenance operations
- Special events calendar