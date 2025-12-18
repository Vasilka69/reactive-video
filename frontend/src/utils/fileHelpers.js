export const getFileUrl = (file) => {
    return `/api/v1/reactive/file/sync/${file.fileId}`;
};

export const getFileType = (file) => {
    const ext = file.filePath.split(".").pop().toLowerCase();
    if (["png", "jpg", "jpeg"].includes(ext)) return "image";
    if (["gif"].includes(ext)) return "gif";
    if (["mp4"].includes(ext)) return "video";
    if (["txt"].includes(ext)) return "text";
    return "other";
};
