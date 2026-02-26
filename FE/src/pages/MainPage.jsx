import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch } from "../api";
import "./MainPage.css";
import gdgocLogo from "./gdgoclogo.png"

function MainPage() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [createOpen, setCreateOpen] = useState(false);

  const [form, setForm] = useState({
    title: "",
    description: "",
    maxMembers: 6,
    recruitmentStartDate: "",
    recruitmentEndDate: "",
    studyStartDate: "",
    studyEndDate: "",
  });

  //화면에 보여줄 모집글 목록 데이터
  const [studies, setStudies] = useState([]);

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

  const extractBoardList = (data) => {
    if (Array.isArray(data)) return data;
    if (Array.isArray(data?.content)) return data.content;
    return [];
  };

  // 모집글 목록 불러오기 (백엔드 연동 시 사용)
  useEffect(() => {
    const fetchBoards = async () => {
      try {
        const data = await apiFetch("/boards/recruiting", { skipAuth: true });
        const mapped = extractBoardList(data).map(mapBoardToStudy);
        // 서버에서 받아온 결과를 반영합니다. 빈 배열이면 화면을 비워둡니다.
        setStudies(mapped);
      } catch (e) {
        console.error("fetchBoards error:", e);
        // 오류 발생 시 빈 목록을 유지합니다.
        setStudies([]);
      }
    };
    fetchBoards();
  }, []);

  // 로그아웃
  const handleLogout = () => {
    localStorage.removeItem("user");
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    setUser(null);
    navigate("/");
};

  const formatDateOnly = (value) => {
    if (!value) return "";
    return String(value).split("T")[0];
  };

  const mapBoardToStudy = (b) => ({
    id: b.boardId ?? b.id,
    title: b.title ?? "",
    description: b.content ?? b.description ?? "",
    leader: b.leader ?? b.writerName ?? b.authorName ?? "알 수 없음",
    members: b.currentCount ?? b.members ?? 0,
    maxMembers: b.capacity ?? b.maxMembers ?? 0,
    date:
      b.recruitmentStartDate && b.recruitmentEndDate
        ? `${formatDateOnly(b.recruitmentStartDate)} ~ ${formatDateOnly(b.recruitmentEndDate)}`
        : "",
    status: b.status === "RECRUITING" ? "모집중":"모집완료"
  });


  // 필터 상태: 전체/모집중/모집완료
  const [filter, setFilter] = useState("전체");

  // 필터링된 스터디 목록
  const filteredStudies =
    filter === "전체"
      ? studies
      : studies.filter((s) => s.status === filter);

  // 모달 상태: 어떤 스터디를 보여줄지 id로 관리
  const [modalStudyId, setModalStudyId] = useState(null);
  const modalStudy = studies.find(s => s.id === modalStudyId);

  // 스터디 가입 버튼 클릭 핸들러
  const handleJoinStudy = async () => {
    if (!user) {
      navigate("/login");
      return;
    }

    if (!modalStudyId) {
      alert("잘못된 접근입니다.");
      return;
    }

    try {
      await apiFetch(`/applications/boards/${modalStudyId}`, {
        method: "POST",
      });
      alert("스터디 가입 신청이 완료되었습니다!");
      setModalStudyId(null);
      // TODO: 필요시 내 스터디 목록/상태 갱신
    } catch (e) {
      if (e?.message?.includes("-401") || e?.message?.includes("본인의 게시글")) {
        alert("본인 글에는 가입할 수 없습니다.");
      } else {
        alert(e?.message || "스터디 가입 실패");
      }
    }
  };

  //글 작성 등록 버튼 클릭 핸들러 
  const handleCreateStudy = async () => {
    if (!user) {
      alert("로그인 후 작성할 수 있습니다.");
      navigate("/login");
      return;
    }

    if (!form.title.trim()) return alert("제목을 입력하세요.");
    if (!form.description.trim()) return alert("설명을 입력하세요.");
    if (!form.maxMembers || form.maxMembers < 1)
      return alert("최대 인원은 1명 이상이어야 해요.");

    if (!form.recruitmentStartDate) return alert("모집 시작일을 입력하세요.");
    if (!form.recruitmentEndDate) return alert("모집 종료일을 입력하세요.");
    if (form.recruitmentEndDate < form.recruitmentStartDate)
      return alert("모집 종료일은 시작일 이후여야 해요.");
        if (!form.studyStartDate) return alert("스터디 시작일을 입력하세요.");
    if (!form.studyEndDate) return alert("스터디 종료일을 입력하세요.");
    if (form.studyEndDate < form.studyStartDate)
      return alert("스터디 종료일은 시작일 이후여야 해요.");
    if (form.recruitmentEndDate > form.studyStartDate)
      return alert("스터디 시작일은 모집 종료일 이후여야 해요.");

    try {
      const payload = {
        title: form.title,
        content: form.description,                  
        capacity: Number(form.maxMembers),         
        recruitmentStartDate: `${form.recruitmentStartDate}T00:00:00`,
        recruitmentEndDate: `${form.recruitmentEndDate}T23:59:59`,
        studyStartDate: `${form.studyStartDate}T00:00:00`,
        studyEndDate: `${form.studyEndDate}T23:59:59`,
      };

      await apiFetch("/boards/create", {
        method: "POST",
        body: JSON.stringify(payload),
      });

      // 새로 작성한 글을 로컬 상태에 즉시 추가합니다.
      const tempId = `local-${Date.now()}`;
      const localStudy = {
        id: tempId,
        title: form.title,
        description: form.description,
        leader: user?.name ?? "알 수 없음",
        members: 1,
        maxMembers: Number(form.maxMembers),
        date: `${form.recruitmentStartDate} ~ ${form.recruitmentEndDate}`,
        status: "모집중",
      };
      setStudies((prev) => [localStudy, ...(Array.isArray(prev) ? prev : [])]);

      // 서버에서 최신 목록을 받아오면 화면을 갱신합니다. 서버가 빈 배열이면 로컬 상태를 유지합니다.
      try {
        const latest = await apiFetch("/boards/recruiting", { skipAuth: true });
        const mappedLatest = extractBoardList(latest).map(mapBoardToStudy);
        // 서버가 빈 배열을 반환하더라도 방금 추가한 로컬 항목은 유지합니다.
        setStudies((prev) => (mappedLatest.length > 0 ? mappedLatest : prev));
      } catch (e) {
        console.error("Failed to refresh studies after create:", e);
        // 네트워크 실패 시 추가한 로컬 항목은 유지합니다.
      }

      setCreateOpen(false);
      setForm({
         title: "", 
         description: "", 
         maxMembers: 6, 
         recruitmentStartDate: "",
         recruitmentEndDate: "",
         studyStartDate: "",
         studyEndDate: "",});

      alert("모집글이 등록되었습니다!");
    } catch (e) {
      alert(e?.message || "모집글 등록 실패");
    }
  };

  return (
    <div className="main-container">
      <header className="main-header">
        <div className="brand">
          <img src={gdgocLogo} alt="GDGoc" className="brand-logo" />
          <span className="brand-title">GDGOC 스터디 게시판</span>
        </div>

        <nav className="main-nav">
          <Link to="/">홈</Link>
          <Link to="/my">내 스터디</Link>
        </nav>

        <div className="user-actions">
          {user ? (
            <>
              <span className="user-name">{user.name}님</span>
              <button onClick={handleLogout} className="btn">로그아웃</button>
            </>
          ) : (
            <>
              <button onClick={() => navigate("/login")} className="btn">로그인</button>
              <button onClick={() => navigate("/register")} className="btn">회원가입</button>
            </>
          )}
        </div>
      </header>

      <div className="banner">GDGoC 스터디 모집글을 확인하고, 원하는 스터디에 지원하세요!</div>

      <main className="main-content">
        <section className="board-section">
          <div className="section-header">
            <h3 className="section-title">스터디 모집 게시판</h3>

            <button
              onClick={() => {
                if (!user) {
                  alert("로그인 후 작성할 수 있습니다.");
                  navigate("/login");
                  return;
                }
                setCreateOpen(true);
              }}
              className="btn btn-primary"
            >
              모집글 작성
            </button>
          </div>

          {/* 필터 탭 */}
          <div className="filter-tabs">
            {["전체", "모집중", "모집완료"].map((tab) => (
              <button
                key={tab}
                onClick={() => setFilter(tab)}
                className={`filter-tab ${filter === tab ? "active" : ""}`}
              >
                {tab}
              </button>
            ))}
          </div>

          {/* 스터디 카드 목록 */}
          <div className="study-grid">
            {filteredStudies.length === 0 ? (
              <div className="study-empty">아직 모집글이 없습니다</div>
            ) : filteredStudies.map((study) => (
              <div
                key={study.id}
                className="study-card"
                onClick={() => {
                  if (!user) {
                    alert("상세보기 및 가입은 로그인 후 가능합니다.");
                    navigate("/login");
                  } else {
                    setModalStudyId(study.id);
                  }
                }}
              >
                <div className="study-title">{study.title}</div>
                <div className="study-desc">{study.description ?? ""}</div>
                <div className="study-meta"><b>리더:</b> {study.leader}</div>
                <div className="study-meta"><b>인원:</b> {study.members} / {study.maxMembers}</div>
                <div className="study-meta"><b>모집 기간:</b> {study.date}</div>
                <div className={`study-status ${study.status === "모집중" ? "open" : "closed"}`}><b>{study.status}</b></div>
              </div>
            ))}
          </div>

          {/* 상세 모달 */}
          {modalStudy && (
            <div className="modal-overlay" onClick={() => setModalStudyId(null)}>
              <div className="modal-box" onClick={e => e.stopPropagation()}>
                <h2 className="modal-title">{modalStudy.title}</h2>
                <p className="modal-desc">{modalStudy.description}</p>

                <div className="modal-row"><b>리더:</b> {modalStudy.leader}</div>
                <div className="modal-row"><b>인원:</b> {modalStudy.members} / {modalStudy.maxMembers}</div>
                <div className="modal-row"><b>기간:</b> {modalStudy.date}</div>
                <div className="modal-row"><b className={modalStudy.status === "모집중" ? "status-open" : "status-closed"}>{modalStudy.status}</b></div>
                <div className="modal-actions">
                  <button onClick={handleJoinStudy} className="btn btn-primary">스터디 가입</button>
                  <button onClick={() => setModalStudyId(null)} className="btn btn-secondary">닫기</button>
                </div>
              </div>
            </div>
          )}

          {/* 글 작성 모달 */}
          {createOpen && (
            <div className="modal-overlay" onClick={() => setCreateOpen(false)}>
              <div className="modal-box modal-form" onClick={(e) => e.stopPropagation()}>
                <h2 className="modal-title">모집글 작성</h2>

                {/* 제목 */}
                <div className="form-row">
                  <div className="form-label">스터디 제목</div>
                  <input
                    value={form.title}
                    onChange={(e) => setForm({ ...form, title: e.target.value })}
                    placeholder="예) Spring Boot 스터디"
                    className="form-input"
                  />
                </div>

                {/* 설명 */}
                <div className="form-row">
                  <div className="form-label">스터디 설명</div>
                  <textarea
                    value={form.description}
                    onChange={(e) => setForm({ ...form, description: e.target.value })}
                    placeholder="스터디 소개를 적어주세요!"
                    rows={5}
                    className="form-textarea"
                  />
                </div>

                {/* 최대 인원 */}
                <div className="form-row">
                  <div className="form-label">최대 인원</div>
                  <input
                    type="number"
                    min={1}
                    value={form.maxMembers}
                    onChange={(e) => setForm({ ...form, maxMembers: Number(e.target.value) })}
                    placeholder="예) 8"
                    className="form-input"
                  />
                </div>

                {/* 모집 시작일 */}
                <div className="form-row">
                  <div className="form-label">모집 시작일</div>
                  <input
                    type="date"
                    value={form.recruitmentStartDate}
                    onChange={(e) =>
                      setForm({ ...form, recruitmentStartDate: e.target.value })
                    }
                    className="form-input"
                  />
                </div>

                {/* 모집 종료일 */}
                <div className="form-row">
                  <div className="form-label">모집 종료일</div>
                  <input
                    type="date"
                    value={form.recruitmentEndDate}
                    onChange={(e) =>
                      setForm({ ...form, recruitmentEndDate: e.target.value })
                    }
                    className="form-input"
                  />
                </div>

                {/* 스터디 시작일 */}
                <div className="form-row">
                  <div className="form-label">스터디 시작일</div>
                  <input
                    type="date"
                    value={form.studyStartDate}
                    onChange={(e) =>
                      setForm({ ...form, studyStartDate: e.target.value })
                    }
                    className="form-input"
                  />
                </div>

                {/* 스터디 종료일 */}
                <div className="form-row">
                  <div className="form-label">스터디 종료일</div>
                  <input
                    type="date"
                    value={form.studyEndDate}
                    onChange={(e) =>
                      setForm({ ...form, studyEndDate: e.target.value })
                    }
                    className="form-input"
                  />
                </div>


                <div className="modal-actions">
                  <button onClick={handleCreateStudy} className="btn btn-primary">등록</button>

                 <button
                    onClick={() => {
                      setCreateOpen(false);
                      setForm({
                        title: "",
                        description: "",
                        maxMembers: 6,
                        recruitmentStartDate: "",
                        recruitmentEndDate: "",
                        studyStartDate: "",
                        studyEndDate: "",
                      });
                    }}
                    className="btn btn-secondary"
                  >
                    닫기
                  </button>

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
