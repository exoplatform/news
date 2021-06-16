export function convertDate(date) {
  let formattedDate = null;
  formattedDate = `${
    pad(date.getMonth()+1) }/${
    pad(date.getDate()) }/${
    pad(date.getFullYear()) } ${
    pad(date.getHours())  }:${
    pad(date.getMinutes())  }:${
    pad(date.getSeconds()) } ${
    getUserTimezone()
  }`;
  return formattedDate;
}

export function pad(n) {
  return n < 10 && `0${n}` || n;
}

export function getUserTimezone() {
  const timeZoneOffset = - (new Date().getTimezoneOffset());
  let timezoneHours = timeZoneOffset / 60;
  let timezoneMinutes = timeZoneOffset % 60;
  timezoneHours = timezoneHours < 10 ? `0${timezoneHours}` : timezoneHours;
  timezoneMinutes = timezoneMinutes < 10 ? `0${timezoneMinutes}` : timezoneMinutes;
  const timezoneSign = timeZoneOffset >= 0 ? '+' : '-';
  return `${timezoneSign}${timezoneHours}${timezoneMinutes}`;
}