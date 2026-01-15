import axios, { CanceledError } from 'axios';

const apiClient =  axios.create({
    baseURL: 'https://backend.gitpushforce-ubb.com',
    // withCredentials: true, // Temporarily disabled to test CORS
})

apiClient.interceptors.request.use((config) => {
    const token = sessionStorage.getItem('access_token');
    if(token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
})

export default apiClient;
export {CanceledError};