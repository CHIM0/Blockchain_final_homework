import axios from 'axios';

const ax = axios.create({
  timeout: 20000,
  withCredentials: false
  // baseURL: config.address
});
ax.interceptors.response.use(
  res => res,
  err => {
    console.log(err.response);
    if (err.response) {
      // The request was made and the server responded with a status code
      // that falls out of the range of 2xx
      console.log(err.response.data);
      console.log(err.response.status);
      console.log(err.response.headers);
    } else if (err.request) {
      // The request was made but no response was received
      // `error.request` is an instance of XMLHttpRequest in the browser and an instance of
      // http.ClientRequest in node.js
      console.log(err.request);
    } else {
      // Something happened in setting up the request that triggered an Error
      console.log('Error', err.message);
    }
    console.log(err.config);
    if (err.message && err.message.match(/timeout/)) {
      const config = err.config;
      if (config.method !== 'get') return Promise.reject(err); // only retry GET methods
      if (config.leftRetry) config.leftRetry--;
      else config.leftRetry = 3; // default retry times

      if (config.leftRetry) {
        return ax(config);
      } else {
        return Promise.reject(err);
      }
    }
    return Promise.reject(err);
  }
);

export default ax;