import React, { useEffect, useState } from "react";
import { apiFetch } from "../api";
import "./MyStudyPage.css";

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
  status: b.status === "RECRUITING" ? "모집중" : "모집완료"
});

const extractBoardList = (data) => {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.content)) return data.content;
  return [];
};

const MyStudyPage = () => {
  const [studies, setStudies] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchBoards = async () => {
      try {
        const data = await apiFetch("/boards/recruiting", { skipAuth: true });
        const mapped = extractBoardList(data).map(mapBoardToStudy);
        setStudies(mapped);
      } catch (e) {
        setStudies([]);
      } finally {
        setLoading(false);
      }
    };
    fetchBoards();
  }, []);

  return (
      <div className="mystudy-container">
        <div className="mystudy-card">
          <div className="mystudy-title-row">
            <div className="mystudy-title-left">
              <h2 className="mystudy-title">내 스터디</h2>
                <div className="mystudy-sub">나의 스터디를 확인세요!
                </div>
            </div>
          </div>

          {loading ? (
            <div className="mystudy-state">불러오는 중...</div>
          ) : studies.length === 0 ? (
            <div className="mystudy-empty">모집 중인 스터디가 없습니다.</div>
          ) : (
            <div className="mystudy-grid">
              {studies.map((study) => (
                <div key={study.id} className="mystudy-item">
                  <div className="mystudy-item-title">{study.title}</div>
                  <div className="mystudy-item-desc">{study.description ?? ""}</div>

                  <div className="mystudy-meta"><b>리더:</b> {study.leader}</div>
                  <div className="mystudy-meta"><b>인원:</b> {study.members} / {study.maxMembers}</div>
                  <div className="mystudy-meta"><b>모집 기간:</b> {study.date}</div>

                  <div className={`mystudy-status ${study.status === "모집중" ? "open" : "closed"}`}>
                    {study.status === "모집중" ? "🟢 모집중" : "⚪ 모집완료"}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    );
  };    
export default MyStudyPage;
