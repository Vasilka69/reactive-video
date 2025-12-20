import React, {useEffect, useState} from "react";
import {Button, Input, message, Select, Space} from "antd";
import {createBucket, deleteBucket, getAllBuckets} from "../../api/buckets";

export default function BucketSelector({ selectedBucket, setSelectedBucket }) {
  const [buckets, setBuckets] = useState([]);
  const [newBucket, setNewBucket] = useState("");
  const [loading, setLoading] = useState(false);

  const fetchBuckets = async () => {
    try {
      const data = await getAllBuckets();
      setBuckets(data);
      if (!selectedBucket && data.length > 0) setSelectedBucket(data[0]);
    } catch (e) {
      console.error(e);
      message.error("Ошибка при получении бакетов");
    }
  };

  useEffect(() => { fetchBuckets(); }, []);

  const handleCreateBucket = async () => {
    if (!newBucket) return;
    try {
      setLoading(true);
      await createBucket(newBucket);
      message.success(`Бакет "${newBucket}" создан`);
      setNewBucket("");
      fetchBuckets();
    } catch (e) {
      console.error(e);
      message.error("Не удалось создать бакет");
    } finally { setLoading(false); }
  };

  const handleDeleteBucket = async (bucket) => {
    try {
      await deleteBucket(bucket);
      message.success(`Бакет "${bucket}" удалён`);
      fetchBuckets();
    } catch (e) {
      console.error(e);
      message.error("Не удалось удалить бакет");
    }
  };

  return (
    <Space style={{ marginBottom: 16 }}>
      <Select
        value={selectedBucket}
        onChange={setSelectedBucket}
        style={{ width: 200 }}
      >
        {buckets.map(b => (
          <Select.Option key={b} value={b}>
            {b}
            <Button
              type="link"
              danger
              size="small"
              onClick={(e) => { e.stopPropagation(); handleDeleteBucket(b); }}
            >
              X
            </Button>
          </Select.Option>
        ))}
      </Select>
      <Input
        placeholder="Новый бакет"
        value={newBucket}
        onChange={e => setNewBucket(e.target.value)}
        style={{ width: 150 }}
      />
      <Button type="primary" onClick={handleCreateBucket} loading={loading}>
        Создать
      </Button>
    </Space>
  );
}
