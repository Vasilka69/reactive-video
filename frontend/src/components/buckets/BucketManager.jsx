import React, {useEffect, useState} from "react";
import {Button, Card, Input, message, Popconfirm, Select, Space} from "antd";
import {createBucket, deleteBucket, getBuckets} from "../../api/buckets";

export default function BucketManager({ selectedBucket, onSelectBucket }) {
    const [buckets, setBuckets] = useState([]);
    const [newBucket, setNewBucket] = useState("");

    const loadBuckets = async () => {
        try {
            const list = await getBuckets();
            setBuckets(list);
        } catch (e) {
            console.error(e);
            message.error("Не удалось загрузить список бакетов");
        }
    };

    useEffect(() => {
        loadBuckets();
    }, []);

    const handleCreate = async () => {
        if (!newBucket.trim()) return;

        try {
            await createBucket(newBucket);
            message.success("Бакет создан");
            setNewBucket("");
            await loadBuckets();
        } catch (e) {
            console.error(e);
            message.error(
                "Ошибка создания бакета: " +
                    (e?.response?.data?.message || "Неизвестная ошибка")
            );
        }
    };

    const handleDelete = async (bucket) => {
        try {
            await deleteBucket(bucket);
            message.success("Бакет удалён");
            await loadBuckets();
        } catch (e) {
            console.error(e);
            message.error("Ошибка удаления бакета: " + (e?.response?.data?.message || "Неизвестная ошибка"));
        }
    };

    return (
        <Card style={{ marginBottom: 24 }}>
            <h3>Управление бакетами</h3>

            <Space direction="vertical" style={{ width: "100%" }}>
                {/* Список + выбор бакета */}
                <div>
                    <div style={{ marginBottom: 4 }}>Выбрать бакет:</div>
                    <Select
                        style={{ width: 240 }}
                        value={selectedBucket}
                        onChange={onSelectBucket}
                        options={buckets.map((b) => ({ label: b, value: b }))}
                        placeholder="Выберите бакет"
                    />
                </div>

                {/* Создание бакета */}
                <Space>
                    <Input
                        placeholder="Название бакета"
                        value={newBucket}
                        onChange={(e) => setNewBucket(e.target.value)}
                    />
                    <Button type="primary" onClick={handleCreate}>
                        Создать
                    </Button>
                </Space>

                {/* Удаление бакетов */}
                <div style={{ marginTop: 12 }}>
                    <div style={{ marginBottom: 4 }}>Удалить бакет:</div>
                    <Space wrap>
                        {buckets.map((b) => (
                            <Popconfirm
                                title="Удалить бакет?"
                                onConfirm={() => handleDelete(b)}
                                okText="Да"
                                cancelText="Нет"
                                key={b}
                            >
                                <Button danger>{b}</Button>
                            </Popconfirm>
                        ))}
                    </Space>
                </div>
            </Space>
        </Card>
    );
}
