//토큰 자동으로 붙여주는 fetch 함수 
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api";

export async function apiFetch(path, options = {}) {
  const { skipAuth = false, ...fetchOptions } = options;
  const accessToken = localStorage.getItem("accessToken");

  const headers = {
    ...(fetchOptions.headers || {}),
  };

  if (fetchOptions.body && !headers["Content-Type"]) {
    headers["Content-Type"] = "application/json";
  }

  if (!skipAuth && accessToken) {
    headers.Authorization = `Bearer ${accessToken}`;
  }

  const res = await fetch(`${API_BASE_URL}${path}`, {
    ...fetchOptions,
    headers,
  });

  const contentType = res.headers.get("content-type") || "";
  const data = contentType.includes("application/json")
    ? await res.json().catch(() => null)
    : await res.text().catch(() => "");


  if (!res.ok) {
    let msg = "요청 실패";

    if (data) {
      if (typeof data === "string") msg = data;
      else if (data.message) msg = data.message;
      else if (data.error) msg = data.error;
      else if (data.errorCode) msg = data.errorCode;
      else msg = JSON.stringify(data);
    }

    throw new Error(msg);
  }


  return data;
}