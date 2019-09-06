export const newsConstants = {
  PORTAL: eXo.env.portal.context || '',
  PORTAL_NAME: eXo.env.portal.portalName || '',
  CONTAINER_NAME: eXo.env.portal.containerName || '',
  PORTAL_REST: eXo.env.portal.rest,
  NEWS_API: `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/news`,
  SOCIAL_ACTIVITY_API: `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/activities`,
  SOCIAL_SPACES_SUGGESTION_API: `${eXo.env.portal.context}/${eXo.env.portal.rest}${eXo.env.portal.context}/social/spaces/suggest.json`,
  SPACE_ID: eXo.env.portal.spaceId,
  HOST_NAME: window.location.host,
  UPLOAD_API: `${eXo.env.portal.context}/upload`,
  MAX_UPLOAD_SIZE: 10,
  MAX_UPLOAD_FILES: 1,
  userName: eXo.env.portal.userName
};
