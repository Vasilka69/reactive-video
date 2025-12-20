import React from "react";
import {Button} from "antd";

export default function ImageCard({ file }) {
  return (
    <div>
      <img src={file.url} style={{ width: "100%", borderRadius: 8 }} />

      <div style={{ marginTop: 12, display: "flex", gap: 12 }}>
        <Button type="primary">Разметить изображение</Button>
        <Button>Использовать кэшированную разметку</Button>
      </div>
    </div>
  );
}
