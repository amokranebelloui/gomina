

## Inventory
List and manage all your projects, environments, instances
- Projects: owners, SCM url, description, code metrics (coverage, loc, etc)
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