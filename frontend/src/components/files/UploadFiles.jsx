import React, {useState} from "react";
import {Button, message, Upload} from "antd";
import {UploadOutlined} from "@ant-design/icons";
import {uploadFile} from "../../api/files";

export default function UploadFiles({ bucket, onUploadSuccess }) {
    const [loading, setLoading] = useState(false);

    const props = {
        customRequest: async ({ file, onSuccess, onError }) => {
            if (!bucket) {
                message.error("Выберите бакет перед загрузкой файла");
                onError();
                return;
            }

            const formData = new FormData();
            formData.append("bucket", bucket);
            formData.append("filePath", "upload");
            formData.append("file", file);

            try {
                setLoading(true);
                await uploadFile(formData);
                message.success(`${file.name} загружен`);
                onSuccess();
                onUploadSuccess();
            } catch (e) {
                console.error(e);
                message.error(
                    "Ошибка загрузки ${file.name}: " +
                        (e?.response?.data?.message || "Неизвестная ошибка")
                );
                onError();
            } finally {
                setLoading(false);
            }
        },
        showUploadList: false,
    };

    return (
        <Upload {...props} style={{ marginBottom: 24 }}>
            <Button
                disabled={!bucket}
                icon={<UploadOutlined />}
                loading={loading}
            >
                Загрузить файл
            </Button>
        </Upload>
    );
}
