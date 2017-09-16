const allInstances = [
    {id: 1, env:'PROD', project:'kernel', service:'kernel', name: 'kernel' , host:'10.143.29.42', folder:'prod-kernel', version:'2.2.0', revision: 34560, status: 'LIVE'},
    {id: 2, env:'PROD', project:'kernel', service:'kernel', name: 'kernel2', host:'10.143.29.41', folder:'prod-kernel', version:'2.1.0', revision: 33982, status: 'LOADING'},
    {id: 3, env:'PROD', project:'basket', service:'basket', name: 'basket' , host:'10.143.3.58', folder:'prod-basket', version:'5.1.3-SNAPSHOT', revision: 34433, status: 'LIVE'},
    {id: 4, env:'PROD', project:'basket', service:'basket', name: 'basket2', host:'10.143.3.56', folder:'prod-basket', version:'5.1.3-SNAPSHOT', revision: 35002, status: 'DOWN'},
    {id: 5, env:'PROD', project:'order',  service:'order',  name: 'order1', host:'10.143.3.58', folder:'prod-order', version:'5.1.3', revision: 35002, status: 'LIVE'},
    {id: 6, env:'PROD', project:'order',  service:'order',  name: 'order2', host:'10.143.3.56', folder:'prod-order', version:'5.1.3-SNAPSHOT', revision: 35002, status: 'DOWN'},
    {id: 7, env:'UAT',  project:'kernel', service:'kernel', name: 'kernel' , host:'10.2.29.42', folder:'uat-kernel', version:'2.3.0-SNAPSHOT', revision: 34561, status: 'LIVE'},
    {id: 8, env:'UAT',  project:'kernel', service:'kernel', name: 'kernel2', host:'10.2.29.41', folder:'uat-kernel', version:'2.3.0-SNAPSHOT', revision: 34560, status: 'LOADING'},
    {id: 9, env:'UAT',  project:'basket', service:'basket', name: 'basket' , host:'10.2.3.58', folder:'uat-basket', version:'5.1.3-SNAPSHOT', revision: 34433, status: 'LIVE'},
    {id: 10, env:'UAT', project:'basket', service:'basket', name: 'basket2', host:'10.2.3.56', folder:'uat-basket', version:'5.1.3-SNAPSHOT', revision: 35014, status: 'DOWN'},
    {id: 11, env:'UAT', project:'fixin',  service:'fixintbx', name: 'fixintbx1', host:'10.2.3.58', folder:'uat-fixintbx', version:'0.9.0-SNAPSHOT', revision: 34433, status: 'LIVE'},
    {id: 12, env:'UAT', project:'fixin',  service:'fixinfid', name: 'fixinfid1', host:'10.2.3.56', folder:'uat-fixinfid', version:'0.9.1-SNAPSHOT', revision: 35014, status: 'DOWN'},

];

const allProjects = [
    {id: 1, name:'kernel', svn:'/svn/kernel', latest: '2.3.0-SNAPSHOT', released: '2.2.0'},
    {id: 2, name:'basket', svn:'/svn/basket', latest: '5.1.3-SNAPSHOT', released: '5.1.2'},
    {id: 3, name:'order',  svn:'/svn/order' , latest: '5.1.3',          released: '5.1.3'},
    {id: 4, name:'fixin',  svn:'/svn/fixin' , latest: '0.9.2-SNAPSHOT', released: '0.9.0'},

];
