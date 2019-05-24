import React from "react";

declare module "react-router-dom" {
    declare module.exports: any;
}
declare module "react-router-dom/es/Link" {
    declare module.exports: any;
}
declare module "react-router-dom/es/withRouter" {
    declare module.exports: any;
}
declare module "react-router-dom/es/Route" {
    declare module.exports: any;
}
declare module 'axios' {
    declare module.exports: any;
}
declare module 'axios/index' {
    declare module.exports: any;
}
declare module 'local-storage' {
    declare module.exports: any;
}
declare module 'react-datepicker' {
    declare module.exports: any;
}
declare module 'react-datepicker/dist/react-datepicker.css' {
    declare module.exports: any;
}
declare module 'query-string' {
    declare module.exports: any;
}
declare module 'd3' {
    declare module.exports: any;
}

declare module '@fortawesome/fontawesome-svg-core' {
    declare module.exports: any;
    //declare var library: any;
    /*
    declare module.exports: {
        library: any;
    };*/
}
declare module "@fortawesome/react-fontawesome/index.es" {
    //declare module.exports: any;
    /**/
    declare module.exports: {
        FontAwesomeIcon: React.Component
    };
    /**/
}
declare module '@fortawesome/free-solid-svg-icons' {
    declare module.exports: any;
}
