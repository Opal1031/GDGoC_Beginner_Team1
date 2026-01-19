import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";

function MainPage() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);

  // 페이지 들어올 때 localStorage에서 로그인 정보 가져오기
  useEffect(() => {
    const saved = localStorage.getItem("user");

    if (saved) {
      try {
        setUser(JSON.parse(saved));
      } catch {
        setUser(null);
      }
    }
  }, []);

  // 로그아웃
  const handleLogout = () => {
    localStorage.removeItem("user");
    setUser(null);
    navigate("/login");
  };

  // 예시 스터디 데이터 2개
  const studies = [
    {
      id: 1,
      title: "Spring Boot 스터디",
      description: "Spring Boot를 처음 배우는 분들을 위한 스터디입니다. 매주 실습과 과제를 통해 백엔드 개발 역량을 키워보세요.",
      status: "모집중",
      leader: "김민제",
      members: 5,
      maxMembers: 8,
      date: "2026-01-20 ~ 2026-03-10"
    },
    {
      id: 2,
      title: "React 스터디",
      description: "React로 실제 웹 서비스를 만들어보는 스터디입니다. 프론트엔드 실무 경험을 쌓고 싶은 분 환영!",
      status: "모집중",
      leader: "김민서",
      members: 4,
      maxMembers: 7,
      date: "2026-02-01 ~ 2026-03-25"
    }
  ];

  // 모달 상태: 어떤 스터디를 보여줄지 id로 관리
  const [modalStudyId, setModalStudyId] = useState(null);
  const modalStudy = studies.find(s => s.id === modalStudyId);

  // 스터디 가입 버튼 클릭 핸들러
  const handleJoinStudy = () => {
    if (!user) {
      navigate("/login");
      return;
    }

    // 실제 가입 로직은 추후 구현
    alert("스터디 가입 기능은 아직 구현되지 않았습니다.");
  };

  return (
    <div className="main-container" style={{ minHeight: "100vh", display: "flex", flexDirection: "column" }}>
      <header style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "20px 40px", borderBottom: "1px solid #eee", background: "#f8f9fa" }}>
        <div style={{ fontWeight: "bold", fontSize: 22 }}>
          GDGoC 스터디 게시판
        </div>

        <nav style={{ display: "flex", gap: 24 }}>
          <Link to="/">홈</Link>
          <Link to="/my">내 스터디</Link>
        </nav>

        <div style={{ display: "flex", gap: 10 }}>
          {user ? (
            <>
              <span style={{ fontWeight: "bold" }}>{user.name}님</span>
              <button onClick={handleLogout} style={{ padding: "8px 16px" }}>로그아웃</button>
            </>
          ) : (
            <>
              <button onClick={() => navigate("/login")} style={{ padding: "8px 16px" }}>로그인</button>
              <button onClick={() => navigate("/register")} style={{ padding: "8px 16px" }}>회원가입</button>
            </>
          )}
        </div>
      </header>

      <div style={{ width: "100%", background: "#e9ecef", padding: "12px 0", textAlign: "center", fontSize: 16, color: "#555" }}>
        GDGoC 스터디 모집글을 확인하고, 원하는 스터디에 지원하세요!
      </div>

      <main style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "flex-start", background: "#fff", paddingTop: 32 }}>
        <section style={{ width: "100%", maxWidth: 900, margin: "0 auto 40px auto", background: "#fff", borderRadius: 8, boxShadow: "0 2px 8px rgba(0,0,0,0.04)", padding: 24 }}>
          <h3 style={{ marginBottom: 16, fontSize: 22, textAlign: "center" }}>
            스터디 모집 게시판
          </h3>
          
          {/* 예시 스터디 카드 2개 */}
          <div style={{ display: "flex", gap: 24, justifyContent: "center" }}>
            {studies.map(study => (
              <div
                key={study.id}
                style={{
                  background: "#f4f6fa",
                  borderRadius: 10,
                  boxShadow: "0 1px 4px rgba(0,0,0,0.06)",
                  padding: 24,
                  minWidth: 260,
                  cursor: "pointer",
                  transition: "box-shadow 0.2s",
                  border: "2px solid #e3e7ed"
                }}

                onClick={() => {
                  if (!user) {
                    alert("상세보기 및 가입은 로그인 후 가능합니다.");
                    navigate("/login");
                  }
                  
                  else {
                    setModalStudyId(study.id);
                  }
                }}
              >
                <div style={{ fontWeight: "bold", fontSize: 18, marginBottom: 8 }}>{study.title}</div>
                <div style={{ color: "#666", fontSize: 14, marginBottom: 6 }}>{study.description.slice(0, 40)}...</div>
                <div style={{ fontSize: 13, marginBottom: 4 }}><b>리더:</b> {study.leader}</div>
                <div style={{ fontSize: 13, marginBottom: 4 }}><b>인원:</b> {study.members} / {study.maxMembers}</div>
                <div style={{ fontSize: 13, marginBottom: 4 }}><b>기간:</b> {study.date}</div>
                <div style={{ fontSize: 13, color: study.status === "모집중" ? "#2b8a3e" : "#888" }}><b>{study.status}</b></div>
              </div>
            ))}
          </div>

          {/* 상세 모달 */}
          {modalStudy && (
            <div style={{
              position: "fixed",
              top: 0,
              left: 0,
              width: "100vw",
              height: "100vh",
              background: "rgba(0,0,0,0.3)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              zIndex: 1000
            }}
              onClick={() => setModalStudyId(null)}
            >

              <div style={{ background: "#fff", borderRadius: 12, padding: 32, minWidth: 340, boxShadow: "0 2px 12px rgba(0,0,0,0.12)" }} onClick={e => e.stopPropagation()}>
                <h2 style={{ marginBottom: 12 }}>{modalStudy.title}</h2>
                <p style={{ color: "#555", marginBottom: 16 }}>{modalStudy.description}</p>

                <div style={{ fontSize: 14, marginBottom: 6 }}><b>리더:</b> {modalStudy.leader}</div>
                <div style={{ fontSize: 14, marginBottom: 6 }}><b>인원:</b> {modalStudy.members} / {modalStudy.maxMembers}</div>
                <div style={{ fontSize: 14, marginBottom: 6 }}><b>기간:</b> {modalStudy.date}</div>
                <div style={{ fontSize: 14, color: modalStudy.status === "모집중" ? "#2b8a3e" : "#888", marginBottom: 16 }}><b>{modalStudy.status}</b></div>
                <div style={{ display: "flex", gap: 12 }}>
                  
                  <button onClick={handleJoinStudy} style={{ padding: "8px 20px", background: "#1976d2", color: "#fff", border: "none", borderRadius: 6, cursor: "pointer" }}>
                    스터디 가입
                  </button>
                  <button onClick={() => setModalStudyId(null)} style={{ padding: "8px 20px", background: "#2b8a3e", color: "#fff", border: "none", borderRadius: 6, cursor: "pointer" }}>닫기</button>
                </div>
              </div>
            </div>
          )}
        </section>
      </main>
    </div>
  );
}

export default MainPage;
