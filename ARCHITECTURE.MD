
#### Entity refs
**component**
- api usage -> api definition (dependency)

**environment**
- service -> component
- instance -> host

**work**
- -> component
- -> user (people involved)


####Packages
model: basic model: components, systems, envs, services, instances, hosts

utils: basic reusable utility functions
integrations: integrations with other systems to decorate the basic model

persistence: persistence of model, and different plugins   

core:
plugins:
api
web

module 
runner