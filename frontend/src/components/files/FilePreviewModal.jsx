import React from "react";
import {Modal} from "antd";

export default function FilePreviewModal({ open, onClose, file, blobUrl }) {
    if (!file) return null;

    return (
        <Modal
            open={open}
            onCancel={onClose}
            footer={null}
            width={900}
            style={{
                top: 20,
                textAlign: "center",
            }}
        >
            <div
                style={{
                    paddingTop: 12,
                    display: "flex",
                    justifyContent: "center",
                }}
            >
                {(file.type === "image" || file.type === "gif") && (
                    <img
                        src={blobUrl}
                        alt={file.filePath}
                        style={{
                            maxWidth: "100%",
                            maxHeight: "80vh",
                            height: "auto",
                            width: "auto",
                            display: "block",
                            objectFit: "contain",
                        }}
                    />
                )}
                {file.type === "video" && (
                    <video
                        src={blobUrl}
                        controls
                        style={{
                            width: "100%",
                            maxHeight: "80vh",
                            display: "block",
                        }}
                    />
                )}
            </div>
        </Modal>
    );
}
