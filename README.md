# Gomina! govern your platform like a boss

Gomina is an application that lets you manage your software artifacts,
in a world where we have more and more granular components. 

Find components, and related information, commit log and branches, 
build pipelines, available versions,
APIs and dependencies, events, etc.   

Analyse library usage, and find simply which components uses which versions

Have a global view on your architecture, with dependency diagrams, 
and a dependency structure matrix 

See deployment topology, hosts and instances

A global overview of projects and features: track work status, involved components,
leverage issue numbers to highlight related commits. 

### Technical
Lib Location

`<test>`

`<script src="https://unpkg.com/react@latest/dist/react.js"></script>`  
`<script src="https://unpkg.com/react-dom@latest/dist/react-dom.js"></script>`  
`<script src="https://unpkg.com/babel-standalone@6.15.0/babel.min.js"></script>`  
`<script src="http://cdn.sockjs.org/sockjs-0.3.4.min.js"></script>`  

    
    
    import React from 'react';
    import ReactDOM from 'react-dom';
    
npm install --save-dev babel-loader babel-core babel-preset-env webpack  
npm install --save react react-dom  

npm install --save-dev babel-preset-react babel-preset-es2015  


### Watching
webpack --progress --colors --watch

### Generate stats
webpack --profile --json > stats.json


npm install -g flow-typed
flow-typed install react-router-dom
flow-typed install axios
