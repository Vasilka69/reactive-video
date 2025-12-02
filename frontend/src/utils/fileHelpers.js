export const getFileUrl = (file) => {
    return `http://localhost:8081/api/v1/reactive/file/sync/${file.fileId}`;
};

export const getFileType = (file) => {
    const ext = file.filePath.split(".").pop().toLowerCase();
    if (["jpg", "jpeg", "png", "gif", "bmp", "webp"].includes(ext))
        return "image";
    if (["mp4", "mov", "avi", "webm", "mkv"].includes(ext)) return "video";
    return "other";
};
