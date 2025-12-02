import {useEffect, useState} from "react";
import axios from "axios";

export const useFileBlob = (fileId) => {
    const [blobUrl, setBlobUrl] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let isMounted = true;
        const fetchFile = async () => {
            try {
                const token = localStorage.getItem("token");
                const res = await axios.get(
                    `http://localhost:8081/api/v1/reactive/file/sync/${fileId}`,
                    {
                        headers: { Authorization: token },
                        responseType: "blob",
                    }
                );
                if (isMounted) {
                    const url = URL.createObjectURL(res.data);
                    setBlobUrl(url);
                }
            } catch (e) {
                console.error("Ошибка загрузки файла:", e);
            } finally {
                if (isMounted) setLoading(false);
            }
        };

        fetchFile();

        return () => {
            isMounted = false;
            if (blobUrl) URL.revokeObjectURL(blobUrl);
        };
    }, [fileId]);

    return { blobUrl, loading };
};
