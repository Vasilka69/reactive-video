import axios from "axios";

const api = axios.create({
    baseURL: "/api/v1/reactive/image-recognition",
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    if (token) config.headers["Authorization"] = token;
    return config;
});

export const recognizeImage = async (fileId) => {
    const res = await api.get(`/${fileId}`);
    return res.data;
};

export const cachedRecognizeImage = async (fileId) => {
    const res = await api.get(`/cached/${fileId}`);
    return res.data;
};
