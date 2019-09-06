import Router from 'vue-router';
import ExoNewsActivityComposer from '../components/ExoNewsActivityComposer.vue';

Vue.use(Router);

const baseUrl= eXo.env.server.portalBaseURL;
let spaceName = '';
if(baseUrl.includes('/g/:spaces:')){
  spaceName = baseUrl.split('/g/:spaces:')[1].split('/')[0];
}
export default new Router({
  base: `/portal/g/:spaces:${spaceName}/${eXo.env.portal.selectedNodeUri}#news`,
  mode: 'history',
  routes: [
    {
      path: '/:action/',
      name: 'NewsComposer',
      component: ExoNewsActivityComposer
    },
    {
      path: '/:action/:nodeId',
      name: 'NewsDraft',
      component: ExoNewsActivityComposer
    }
  ]
});
