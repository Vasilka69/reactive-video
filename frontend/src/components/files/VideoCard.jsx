import React from "react";

export default function VideoCard({ file }) {
    return (
        <video
            controls
            src={file.url}
            style={{ width: "600px", maxWidth: "100%", borderRadius: 8 }}
        />
    );
}
