import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8081/api/v1/reactive",
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    if (token) {
        config.headers["Authorization"] = token;
    }
    return config;
});

export const getAllFiles = async () => {
    const res = await api.get("/file-metadata");
    return res.data;
};

export const getFileById = async (id) => {
    const res = await api.get(`/file-metadata/${id}`);
    return res.data;
};

export const uploadFile = async (formData) => {
    const res = await api.post("/file", formData, {
        headers: { "Content-Type": "multipart/form-data" },
    });
    return res.data;
};

export const deleteFile = async (id) => {
    const res = await api.delete(`/file/${id}`);
    return res.data;
};
