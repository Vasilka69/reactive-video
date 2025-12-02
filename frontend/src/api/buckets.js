import axios from "./axios";

export async function getBuckets() {
  const res = await axios.get("/s3-bucket");
  return res.data;
}

export async function createBucket(bucketName) {
  return axios.post(`/s3-bucket?bucketName=${bucketName}`);
}

export async function deleteBucket(bucketName) {
  return axios.delete(`/s3-bucket?bucketName=${bucketName}`);
}

export async function bucketExists(bucketName) {
  const res = await axios.get(`/s3-bucket/exists?bucketName=${bucketName}`);
  return res.data;
}
