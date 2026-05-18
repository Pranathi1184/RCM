import axios from "axios";

const aiApi = axios.create({
  baseURL: "/"
});

export default aiApi;