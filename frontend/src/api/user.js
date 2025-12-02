import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8081/api/v1/reactive/user",
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    if (token) {
        config.headers["Authorization"] = token;
    }
    return config;
});

export async function fetchUser() {
    const res = await api.get();
    return res.data;
}
