import React from "react";
import {Modal} from "antd";

export default function FilePreviewModal({ open, onClose, file, blobUrl }) {
    if (!file) return null;

    return (
        <Modal
            open={open}
            onCancel={onClose}
            footer={null}
            width={850}
            style={{
                top: 20,
                textAlign: "center",
            }}
        >
            <div
                style={{
                    paddingTop: 30,
                }}
            >
                {file.type === "image" && (
                    <img
                        src={blobUrl}
                        alt={file.filePath}
                        style={{
                            width: "100%",
                            height: "auto",
                            display: "block",
                            margin: 0,
                        }}
                    />
                )}
                {file.type === "video" && (
                    <video
                        src={blobUrl}
                        controls
                        style={{
                            width: "100%",
                            maxHeight: "90vh",
                            display: "block",
                        }}
                    />
                )}
            </div>
        </Modal>
    );
}
