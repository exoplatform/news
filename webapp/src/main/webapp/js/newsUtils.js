export function convertDate(date) {
  return `${
    pad(date.getMonth()+1) }/${
    pad(date.getDate()) }/${
    pad(date.getFullYear()) } ${
    pad(date.getHours())  }:${
    pad(date.getMinutes())  }:${
    pad(date.getSeconds()) }`;
}
export function convertScheduleDate(date) {
  return `${
    pad(date.getFullYear())}-${
    pad(date.getMonth()+1)}-${
    pad(date.getDate())}T${
    pad(date.getHours())}:${
    pad(date.getMinutes())}:${
    pad(date.getSeconds())}.${
    pad(date.getMilliseconds())}0${
    pad(getUserTimezone(date))}`;
}
export function getUserTimezone(date) {
  const timeZoneOffset = - (date.getTimezoneOffset());
  let timezoneHours = timeZoneOffset / 60;
  let timezoneMinutes = timeZoneOffset % 60;
  timezoneHours = timezoneHours < 10 ? `0${timezoneHours}` : timezoneHours;
  timezoneMinutes = timezoneMinutes < 10 ? `0${timezoneMinutes}` : timezoneMinutes;
  const timezoneSign = timeZoneOffset >= 0 ? '+' : '-';
  return `${timezoneSign}${timezoneHours}:${timezoneMinutes}`;
}

export function pad(n) {
  return n < 10 && `0${n}` || n;
}