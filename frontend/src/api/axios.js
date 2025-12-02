import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8081/api/v1",
});

api.interceptors.request.use((config) => {
    try {
        const token = localStorage.getItem("token");
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
    } catch (e) {
        console.warn("localStorage недоступен:", e);
    }
    return config;
});

export default api;
