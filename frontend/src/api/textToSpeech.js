import axios from "axios";

const api = axios.create({
    baseURL: "/api/v1/reactive/text-to-speech",
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    if (token) config.headers["Authorization"] = token;
    return config;
});

const fetchAudio = async (path) => {
    const res = await api.get(path, { responseType: "blob" });
    return res.data;
};

export const textToSpeech = async (fileId) => {
    return fetchAudio(`/${fileId}`);
};

export const cachedTextToSpeech = async (fileId) => {
    return fetchAudio(`/cached/${fileId}`);
};
