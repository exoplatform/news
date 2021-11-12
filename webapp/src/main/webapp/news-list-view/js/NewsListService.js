export function getNewsList(newsTarget, limit) { // eslint-disable-line no-unused-vars
  // TODO
  return Promise.resolve([]);
}

export function getNewsTarget() { // eslint-disable-line no-unused-vars
  // TODO
  return Promise.resolve(['snapshotSliderNews', 'snapshotLatestNews']);
}

export function saveSettings(saveSettingsURL ,settings) {
  const formData = new FormData();
  if (settings) {
    Object.keys(settings).forEach(name => {
      formData.append(name, settings[name]);
    });
  }
  return fetch(saveSettingsURL, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams(formData).toString(),
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}