import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./auth.css";

const API_BASE_URL = "http://localhost:8080";

function LoginPage() {
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg("");

    if (!email.trim() || !password.trim()) {
      setErrorMsg("이메일과 비밀번호를 입력해주세요.");
      return;
    }

    try {
      setLoading(true);

      const res = await fetch(`${API_BASE_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ loginEmail: email, password }),
      });

      let data = null;
      let text = "";

      const contentType = res.headers.get("content-type") || "";
      if (contentType.includes("application/json")) {
        data = await res.json().catch(() => null);
      } else {
        text = await res.text().catch(() => "");
      }

      if (!res.ok) {
        const msg = (data && data.message) || text || "로그인 실패";
        throw new Error(msg);
      }

      if (data) {
        localStorage.setItem("user", JSON.stringify(data));
      }

      navigate("/");
    } catch (err) {
      setErrorMsg(err?.message || "로그인 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <h1 className="auth-title">로그인</h1>

      <form onSubmit={handleSubmit} className="auth-form">
        <label className="auth-label">
          이메일
          <input
            className="auth-input"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="email@example.com"
            autoComplete="email"
            disabled={loading}
          />
        </label>

        <label className="auth-label">
          비밀번호
          <input
            className="auth-input"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="비밀번호 입력"
            autoComplete="current-password"
            disabled={loading}
          />
        </label>

        {errorMsg && <p className="auth-msg-error">{errorMsg}</p>}

        <div className="auth-actions">
          <button className="auth-button" type="submit" disabled={loading}>
            {loading ? "로그인 중.." : "로그인"}
          </button>

          <button
            className="auth-button"
            type="button"
            onClick={() => navigate("/register")}
            disabled={loading}
          >
            회원가입
          </button>
          <button
            className="auth-button"
            type="button"
            onClick={() => navigate("/")}
          >
            홈으로 돌아가기
          </button>
        </div>
      </form>
    </div>
  );
}

export default LoginPage;
