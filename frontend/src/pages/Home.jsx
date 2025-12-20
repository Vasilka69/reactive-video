import React, {useEffect, useState} from "react";
import AppLayout from "../components/layout/AppLayout";
import FileCard from "../components/files/FileCard";
import {deleteFile, getAllFiles} from "../api/files";
import {message, Spin} from "antd";
import {getFileType, getFileUrl} from "../utils/fileHelpers";
import UploadFiles from "../components/files/UploadFiles";
import BucketManager from "../components/buckets/BucketManager";

export default function Home() {
    const [files, setFiles] = useState([]);
    const [loading, setLoading] = useState(true);
    const [bucket, setBucket] = useState(null);

    const fetchFiles = async () => {
        try {
            const data = await getAllFiles();
            const mapped = data.map((f) => ({
                ...f,
                type: getFileType(f),
                url: getFileUrl(f),
            }));
            setFiles(mapped);
        } catch (e) {
            console.error(e);
            message.error("Ошибка при загрузке файлов");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchFiles();
    }, []);

    const handleDelete = async (id) => {
        try {
            await deleteFile(id);
            setFiles((prev) => prev.filter((f) => f.fileId !== id));
            message.success("Файл удалён");
        } catch (e) {
            console.error(e);
            message.error("Не удалось удалить файл");
        }
    };

    const handleUploadSuccess = () => {
        fetchFiles();
    };

    if (loading) return <Spin size="large" style={{ marginTop: 100 }} />;

    return (
        <AppLayout>
            <BucketManager selectedBucket={bucket} onSelectBucket={setBucket} />
            <UploadFiles
                bucket={bucket}
                onUploadSuccess={handleUploadSuccess}
            />
            {files.map((f) => (
                <FileCard
                    key={f.fileId}
                    file={f}
                    onDelete={() => handleDelete(f.fileId)}
                />
            ))}
        </AppLayout>
    );
}
