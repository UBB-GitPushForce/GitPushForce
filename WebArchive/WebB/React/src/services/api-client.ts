import axios, { CanceledError } from 'axios';

const apiClient =  axios.create({
    baseURL: 'http://localhost:8000',
    withCredentials: true,
})

// apiClient.interceptors.request.use((config) => {
//     const token = sessionStorage.getItem('access_token');
//     if(token) {
//         config.headers.Authorization = `Bearer ${token}`;
//     }
//     return config;
// })

export default apiClient;
export {CanceledError};