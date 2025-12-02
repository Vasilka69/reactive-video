import React, {useEffect, useRef, useState} from "react";

export default function ImageWithAnnotations({
    src,
    annotations = [],
    onSize,
}) {
    const imgRef = useRef();
    const [loaded, setLoaded] = useState(false);

    useEffect(() => {
        if (loaded && imgRef.current) {
            const { naturalWidth, naturalHeight } = imgRef.current;
            onSize && onSize(naturalWidth, naturalHeight);
        }
    }, [loaded]);

    return (
        <div
            style={{
                position: "relative",
                display: "inline-block",
                alignItems: "center",
            }}
        >
            <img
                ref={imgRef}
                src={src}
                alt=""
                style={{
                    width: "100%",
                    display: "block",
                    alignItems: "center",
                    textAlign: "center",
                }}
                onLoad={() => setLoaded(true)}
            />
            {annotations.map((ann, i) => (
                <div
                    key={i}
                    style={{
                        position: "absolute",
                        left: `${ann.x}%`,
                        top: `${ann.y}%`,
                        width: `${ann.width}%`,
                        height: `${ann.height}%`,
                        border: "2px solid red",
                        pointerEvents: "none",
                    }}
                >
                    <span
                        style={{
                            position: "absolute",
                            background: "red",
                            color: "white",
                            fontSize: 12,
                        }}
                    >
                        {ann.label}
                    </span>
                </div>
            ))}
        </div>
    );
}
