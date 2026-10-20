# -*- coding: utf-8 -*-
"""Run thesis §6.3 API tests and dump UTF-8 JSON results. Do not invent outcomes."""
from __future__ import annotations

import json
import os
import time
import uuid
from pathlib import Path
from typing import Any

import urllib.error
import urllib.request

BASE = "http://localhost:8080"
OUT = Path(__file__).resolve().parent / "section63-results.json"
RESULTS: dict[str, Any] = {}


def api(
    method: str,
    path: str,
    *,
    token: str | None = None,
    body: Any = None,
    files: dict[str, tuple[str, bytes, str]] | None = None,
    timeout: int = 180,
) -> dict[str, Any]:
    url = BASE + path
    headers: dict[str, str] = {}
    data: bytes | None = None
    if token:
        headers["Authorization"] = f"Bearer {token}"
    if files:
        boundary = "----Boundary" + uuid.uuid4().hex
        headers["Content-Type"] = f"multipart/form-data; boundary={boundary}"
        parts: list[bytes] = []
        for name, (filename, content, ctype) in files.items():
            parts.append(f"--{boundary}\r\n".encode())
            parts.append(
                f'Content-Disposition: form-data; name="{name}"; filename="{filename}"\r\n'.encode()
            )
            parts.append(f"Content-Type: {ctype}\r\n\r\n".encode())
            parts.append(content)
            parts.append(b"\r\n")
        parts.append(f"--{boundary}--\r\n".encode())
        data = b"".join(parts)
    elif body is not None:
        headers["Content-Type"] = "application/json"
        data = json.dumps(body, ensure_ascii=False).encode("utf-8")
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            raw = resp.read().decode("utf-8")
            status = resp.status
    except urllib.error.HTTPError as e:
        raw = e.read().decode("utf-8", errors="replace")
        status = e.code
    except Exception as e:  # noqa: BLE001
        return {
            "ok": False,
            "status": None,
            "error": str(e),
            "code": None,
            "message": str(e),
            "data": None,
            "raw": None,
        }
    try:
        parsed = json.loads(raw)
    except json.JSONDecodeError:
        parsed = None
    return {
        "ok": status == 200 and (parsed or {}).get("success") is True
        if parsed
        else status == 200,
        "status": status,
        "code": (parsed or {}).get("code"),
        "message": (parsed or {}).get("message"),
        "data": (parsed or {}).get("data"),
        "success": (parsed or {}).get("success"),
        "raw": raw,
    }


def rec(key: str, **kwargs: Any) -> None:
    RESULTS[key] = kwargs
    msg = kwargs.get("message")
    code = kwargs.get("code")
    print(f"[{key}] code={code} message={msg}", flush=True)


def questions_of(data: Any) -> list:
    if not isinstance(data, dict):
        return []
    return data.get("questions") or []


def main() -> None:
    stamp = time.strftime("%Y%m%d%H%M%S")
    RESULTS["meta"] = {
        "base": BASE,
        "stamp": stamp,
        "os": "Windows 11",
    }

    try:
        _run(stamp)
    except Exception as e:  # noqa: BLE001
        RESULTS["fatal_error"] = str(e)
        print(f"FATAL: {e}", flush=True)
    finally:
        OUT.write_text(json.dumps(RESULTS, ensure_ascii=False, indent=2), encoding="utf-8")
        print(f"WROTE {OUT}", flush=True)


def _run(stamp: str) -> None:
    # ---------- 6.1 ----------
    r = api("POST", "/api/auth/admin/login", body={"username": "", "password": ""})
    rec(
        "6.1.1_empty_api",
        code=r["code"],
        message=r["message"],
        note="API Bean 校验；前端另有「请输入用户名/请输入密码」",
        frontend_hint="请输入用户名 / 请输入密码",
    )

    r = api("POST", "/api/auth/admin/login", body={"username": "nosuch", "password": "admin123"})
    rec("6.1.2_bad_user", code=r["code"], message=r["message"])

    r = api("POST", "/api/auth/admin/login", body={"username": "admin", "password": "wrong"})
    rec("6.1.3_bad_pass", code=r["code"], message=r["message"])

    r = api("POST", "/api/auth/admin/login", body={"username": "admin", "password": "admin123"})
    admin_token = (r.get("data") or {}).get("token")
    rec(
        "6.1.4_ok",
        code=r["code"],
        message=r["message"],
        has_token=bool(admin_token),
        realName=(r.get("data") or {}).get("realName"),
    )
    if not admin_token:
        raise RuntimeError(f"admin login failed: {r}")

    r = api("GET", "/api/admin/jobs")
    rec("6.1.5_no_token", code=r["code"], message=r["message"])

    # ---------- 6.2 ----------
    r = api(
        "POST",
        "/api/admin/interviewer-configs",
        token=admin_token,
        body={
            "aiName": f"测试人设-{stamp}",
            "systemPrompt": "你是严格但友善的技术面试官，输出结构化评估。",
        },
    )
    cfg_id = (r.get("data") or {}).get("id") or (r.get("data") if isinstance(r.get("data"), int) else None)
    # list to find if id nested
    if cfg_id is None and isinstance(r.get("data"), dict):
        cfg_id = r["data"].get("id")
    rec("6.2.1_create_config", code=r["code"], message=r["message"], data=r.get("data"))
    if cfg_id is None:
        lst = api("GET", "/api/admin/interviewer-configs", token=admin_token)
        items = lst.get("data") or []
        for it in items:
            if it.get("aiName") == f"测试人设-{stamp}":
                cfg_id = it.get("id")
                break
    if not cfg_id:
        raise RuntimeError(f"config create failed: {r}")

    r = api(
        "POST",
        "/api/admin/jobs",
        token=admin_token,
        body={
            "jobName": f"测试岗位-{stamp}",
            "jobDesc": "论文测试岗位",
            "interviewerConfigId": cfg_id,
            "prompt": "评估 Java 基础与表达。",
            "status": 1,
        },
    )
    job_id = None
    if isinstance(r.get("data"), dict):
        job_id = r["data"].get("id")
    elif isinstance(r.get("data"), (int, float)):
        job_id = int(r["data"])
    rec("6.2.2_create_job", code=r["code"], message=r["message"], data=r.get("data"))
    if job_id is None:
        lst = api("GET", "/api/admin/jobs", token=admin_token)
        for it in lst.get("data") or []:
            if it.get("jobName") == f"测试岗位-{stamp}":
                job_id = it.get("id")
                break
    if not job_id:
        raise RuntimeError(f"job create failed: {r}")

    # candidate login to see open jobs
    cand = api(
        "POST",
        "/api/auth/candidate/login",
        body={"mobile": "13800000001", "password": "candidate123"},
    )
    cand_token = (cand.get("data") or {}).get("token")
    cand_id = (cand.get("data") or {}).get("candidateId")
    rec(
        "6.2.2_user_sees_job",
        code=cand["code"],
        message=cand["message"],
        has_token=bool(cand_token),
    )
    if not cand_token:
        raise RuntimeError(f"candidate login failed: {cand}")

    jobs_open = api("GET", "/api/interview/jobs", token=cand_token)
    open_ids = [j.get("id") for j in (jobs_open.get("data") or [])]
    rec(
        "6.2.2_open_jobs_contains",
        code=jobs_open["code"],
        message=jobs_open["message"],
        contains=job_id in open_ids,
        open_ids=open_ids,
    )

    r = api(
        "PATCH",
        f"/api/admin/jobs/{job_id}/status",
        token=admin_token,
        body={"status": 2},
    )
    rec("6.2.3_close_job", code=r["code"], message=r["message"], data=r.get("data"))
    jobs_open2 = api("GET", "/api/interview/jobs", token=cand_token)
    open_ids2 = [j.get("id") for j in (jobs_open2.get("data") or [])]
    rec(
        "6.2.3_user_hidden",
        code=jobs_open2["code"],
        contains=job_id in open_ids2,
        open_ids=open_ids2,
    )

    # reopen for later use if needed
    api("PATCH", f"/api/admin/jobs/{job_id}/status", token=admin_token, body={"status": 1})

    r = api(
        "POST",
        "/api/admin/questions",
        token=admin_token,
        body={
            "jobId": job_id,
            "question": f"测试题-{stamp}：请介绍 HashMap？",
            "referenceAnswer": "哈希表、扩容、线程安全差异等。",
            "isOn": 1,
            "sortOrder": 1,
        },
    )
    q_id = None
    if isinstance(r.get("data"), dict):
        q_id = r["data"].get("id")
    elif isinstance(r.get("data"), (int, float)):
        q_id = int(r["data"])
    rec("6.2.4_add_question", code=r["code"], message=r["message"], data=r.get("data"))
    if q_id is None:
        ql = api("GET", f"/api/admin/questions?jobId={job_id}", token=admin_token)
        for it in ql.get("data") or []:
            if f"测试题-{stamp}" in (it.get("question") or ""):
                q_id = it.get("id")
                break
    if not q_id:
        raise RuntimeError(f"question create failed: {r}")

    r = api(
        "PATCH",
        f"/api/admin/questions/{q_id}/isOn",
        token=admin_token,
        body={"isOn": 0},
    )
    # fallback path
    if r.get("code") not in (200, None) and r.get("success") is not True:
        r2 = api(
            "PATCH",
            f"/api/admin/questions/{q_id}/status",
            token=admin_token,
            body={"isOn": 0},
        )
        if r2.get("success"):
            r = r2
    rec("6.2.5_disable_question", code=r["code"], message=r["message"], data=r.get("data"), raw=r.get("raw"))
    ql = api("GET", f"/api/admin/questions?jobId={job_id}", token=admin_token)
    disabled = None
    for it in ql.get("data") or []:
        if it.get("id") == q_id:
            disabled = it.get("isOn")
            break
    rec("6.2.5_is_on_value", isOn=disabled)

    new_mobile = f"139{stamp[-8:]}"
    r = api(
        "POST",
        "/api/admin/candidates",
        token=admin_token,
        body={
            "realName": f"测试候选人-{stamp}",
            "mobile": new_mobile,
            "password": "test123456",
            "sex": 1,
        },
    )
    rec("6.2.6_create_candidate", code=r["code"], message=r["message"], data=r.get("data"), mobile=new_mobile)
    login_new = api(
        "POST",
        "/api/auth/candidate/login",
        body={"mobile": new_mobile, "password": "test123456"},
    )
    rec(
        "6.2.6_login_new",
        code=login_new["code"],
        message=login_new["message"],
        has_token=bool((login_new.get("data") or {}).get("token")),
    )

    # close test job again to reduce noise (optional keep open for LLM-only later)
    # keep open for 6.6 zero-bank if we use this job with disabled q

    # ---------- 6.3 isolation ----------
    r = api(
        "POST",
        "/api/auth/candidate/login",
        body={"mobile": "13800000000", "password": "wrong"},
    )
    rec("6.3.1_bad_login", code=r["code"], message=r["message"])

    r = api(
        "POST",
        "/api/auth/candidate/login",
        body={"mobile": "13800000001", "password": "candidate123"},
    )
    cand_a_token = (r.get("data") or {}).get("token")
    cand_a_id = (r.get("data") or {}).get("candidateId")
    rec(
        "6.3.2_ok_login",
        code=r["code"],
        message=r["message"],
        candidateId=cand_a_id,
        has_token=bool(cand_a_token),
    )

    r = api("GET", "/api/admin/jobs", token=cand_a_token)
    rec("6.3.3_cand_to_admin", code=r["code"], message=r["message"])

    # candidate B
    r = api(
        "POST",
        "/api/auth/candidate/login",
        body={"mobile": "13800000002", "password": "candidate123"},
    )
    cand_b_token = (r.get("data") or {}).get("token")
    cand_b_id = (r.get("data") or {}).get("candidateId")
    rec("6.3.4_cand_b_login", code=r["code"], candidateId=cand_b_id)

    # get B resumes
    resumes_b = api("GET", "/api/resumes", token=cand_b_token)
    b_resume_id = None
    for it in resumes_b.get("data") or []:
        b_resume_id = it.get("id")
        break
    # if none, create session for B later; for resume cross-access use detail
    cross = None
    if b_resume_id:
        cross = api("GET", f"/api/resumes/{b_resume_id}/detail", token=cand_a_token)
        rec(
            "6.3.4_cross_resume",
            code=cross["code"],
            message=cross["message"],
            b_resume_id=b_resume_id,
        )
    else:
        # try create session for B and A access
        jobs = api("GET", "/api/interview/jobs", token=cand_b_token)
        jid = (jobs.get("data") or [{}])[0].get("id") or 1
        sess_b = api(
            "POST",
            "/api/interview/sessions",
            token=cand_b_token,
            body={"jobId": jid},
        )
        sid_b = (sess_b.get("data") or {}).get("id") or (sess_b.get("data") or {}).get("sessionId")
        if isinstance(sess_b.get("data"), dict):
            sid_b = sess_b["data"].get("id") or sess_b["data"].get("sessionId")
        cross = api("GET", f"/api/interview/sessions/{sid_b}", token=cand_a_token)
        rec(
            "6.3.4_cross_session",
            code=cross["code"],
            message=cross["message"],
            sid_b=sid_b,
            create_b=sess_b.get("message"),
            create_code=sess_b.get("code"),
        )

    r = api("GET", "/api/resumes", token=admin_token)
    rec("6.3.5_admin_to_resumes", code=r["code"], message=r["message"])
    r = api("GET", "/api/interview/jobs", token=admin_token)
    rec("6.3.5_admin_to_interview", code=r["code"], message=r["message"])

    # ---------- 6.4 resume ----------
    sample_txt = (
        "张同学\n手机：13800000001\n求职意向：Java后端\n"
        "教育经历：某某大学 计算机 本科\n"
        "技能：Java, Spring Boot, MySQL, Redis\n"
        "项目：智能面试平台，负责后端接口与异步任务。\n"
    ).encode("utf-8")
    # make unique content each run for first upload, then duplicate
    unique_txt = sample_txt + f"\n备注:{stamp}\n".encode("utf-8")
    up = api(
        "POST",
        "/api/resumes/upload",
        token=cand_a_token,
        files={"file": (f"resume-{stamp}.txt", unique_txt, "text/plain")},
    )
    resume_id = None
    if isinstance(up.get("data"), dict):
        resume_id = up["data"].get("id") or up["data"].get("resumeId")
    rec(
        "6.4.1_upload",
        code=up["code"],
        message=up["message"],
        data=up.get("data"),
        analyzeStatus=(up.get("data") or {}).get("analyzeStatus")
        if isinstance(up.get("data"), dict)
        else None,
    )
    if not resume_id:
        raise RuntimeError(f"upload failed: {up}")

    # poll
    final_status = None
    detail_data = None
    for i in range(40):
        time.sleep(3)
        d = api("GET", f"/api/resumes/{resume_id}/detail", token=cand_a_token)
        detail_data = d.get("data")
        st = None
        if isinstance(detail_data, dict):
            st = detail_data.get("analyzeStatus") or (detail_data.get("resume") or {}).get(
                "analyzeStatus"
            )
        final_status = st
        print(f"  poll resume {i}: status={st}", flush=True)
        if st in ("COMPLETED", "FAILED", "COMPLETED".lower()):
            break
        # also check list
        lst = api("GET", "/api/resumes", token=cand_a_token)
        for it in lst.get("data") or []:
            if it.get("id") == resume_id:
                final_status = it.get("analyzeStatus")
                if final_status in ("COMPLETED", "FAILED"):
                    detail_data = it
                break
        if final_status in ("COMPLETED", "FAILED"):
            if final_status == "COMPLETED":
                d = api("GET", f"/api/resumes/{resume_id}/detail", token=cand_a_token)
                detail_data = d.get("data")
            break

    rec(
        "6.4.2_analyze_done",
        analyzeStatus=final_status,
        detail_keys=list(detail_data.keys()) if isinstance(detail_data, dict) else None,
        detail=detail_data if final_status == "COMPLETED" else detail_data,
    )

    dup = api(
        "POST",
        "/api/resumes/upload",
        token=cand_a_token,
        files={"file": (f"resume-{stamp}-dup.txt", unique_txt, "text/plain")},
    )
    rec(
        "6.4.3_duplicate",
        code=dup["code"],
        message=dup["message"],
        data=dup.get("data"),
        duplicated=(dup.get("data") or {}).get("duplicated")
        if isinstance(dup.get("data"), dict)
        else None,
    )

    empty = api(
        "POST",
        "/api/resumes/upload",
        token=cand_a_token,
        files={"file": ("empty.txt", b"", "text/plain")},
    )
    rec("6.4.4_empty", code=empty["code"], message=empty["message"])

    bad = api(
        "POST",
        "/api/resumes/upload",
        token=cand_a_token,
        files={"file": ("bad.exe", b"MZ fake", "application/octet-stream")},
    )
    rec("6.4.4_bad_ext", code=bad["code"], message=bad["message"])

    rec("6.4.5_invalid_key", skipped=True, note="未执行（可选负例）")

    # ---------- 6.5 interview flow ----------
    # use seed job 1 (enough bank questions)
    sess = api(
        "POST",
        "/api/interview/sessions",
        token=cand_a_token,
        body={"jobId": 1, "resumeId": resume_id},
    )
    sid = None
    if isinstance(sess.get("data"), dict):
        sid = sess["data"].get("id") or sess["data"].get("sessionId")
    rec("6.5.1_create", code=sess["code"], message=sess["message"], data=sess.get("data"))
    if not sid:
        raise RuntimeError(f"session create failed: {sess}")

    detail = api("GET", f"/api/interview/sessions/{sid}", token=cand_a_token)
    rec(
        "6.5.1_detail",
        status=(detail.get("data") or {}).get("status"),
        totalQuestions=(detail.get("data") or {}).get("totalQuestions"),
        q_count=len((detail.get("data") or {}).get("questions") or []),
    )

    # answer required checks first on a fresh session for "未答" cases - create another
    sess2 = api("POST", "/api/interview/sessions", token=cand_a_token, body={"jobId": 1})
    sid2 = (sess2.get("data") or {}).get("id")
    follow_no = api("POST", f"/api/interview/sessions/{sid2}/follow-up", token=cand_a_token)
    rec("6.5.3_followup_no_answer", code=follow_no["code"], message=follow_no["message"])
    next_no = api("POST", f"/api/interview/sessions/{sid2}/next", token=cand_a_token)
    rec("6.5.3_next_no_answer", code=next_no["code"], message=next_no["message"])
    complete_no = api("POST", f"/api/interview/sessions/{sid2}/complete", token=cand_a_token)
    rec("6.5.7_complete_no_answer", code=complete_no["code"], message=complete_no["message"])

    ans = api(
        "POST",
        f"/api/interview/sessions/{sid}/answers",
        token=cand_a_token,
        body={"answerText": "这是我的第一题作答，包含 equals 与 == 的区别说明。"},
    )
    rec("6.5.2_answer", code=ans["code"], message=ans["message"], data=ans.get("data"))
    ans_dup = api(
        "POST",
        f"/api/interview/sessions/{sid}/answers",
        token=cand_a_token,
        body={"answerText": "重复提交"},
    )
    rec("6.5.2_answer_dup", code=ans_dup["code"], message=ans_dup["message"])

    fu = api("POST", f"/api/interview/sessions/{sid}/follow-up", token=cand_a_token)
    rec("6.5.4_followup", code=fu["code"], message=fu["message"], data=fu.get("data"))
    next_before_fu_ans = api("POST", f"/api/interview/sessions/{sid}/next", token=cand_a_token)
    rec(
        "6.5.4_next_before_follow_answer",
        code=next_before_fu_ans["code"],
        message=next_before_fu_ans["message"],
    )
    fu_ans = api(
        "POST",
        f"/api/interview/sessions/{sid}/follow-up/answer",
        token=cand_a_token,
        body={"answerText": "追问答：补充业务场景。"},
    )
    rec("6.5.4_follow_answer", code=fu_ans["code"], message=fu_ans["message"])

    # next until last
    next_indices = []
    last_next_msg = None
    last_next_code = None
    for _ in range(10):
        n = api("POST", f"/api/interview/sessions/{sid}/next", token=cand_a_token)
        last_next_code, last_next_msg = n["code"], n["message"]
        if not n.get("success"):
            break
        # answer current
        det = api("GET", f"/api/interview/sessions/{sid}", token=cand_a_token)
        cur = (det.get("data") or {}).get("currentIndex")
        next_indices.append(cur)
        a = api(
            "POST",
            f"/api/interview/sessions/{sid}/answers",
            token=cand_a_token,
            body={"answerText": f"第{cur}题作答内容，说明原理与实践。"},
        )
        if not a.get("success"):
            last_next_msg = a["message"]
            last_next_code = a["code"]
            break
    # try next on last after answering last
    n_last = api("POST", f"/api/interview/sessions/{sid}/next", token=cand_a_token)
    rec(
        "6.5.5_next_flow",
        indices=next_indices,
        last_fail_code=n_last["code"] if not n_last.get("success") else last_next_code,
        last_fail_message=n_last["message"] if not n_last.get("success") else last_next_msg,
        final_next_success=n_last.get("success"),
        final_next_message=n_last["message"],
    )

    # ensure at least answered then complete
    # if still mid-way, answer remaining quickly without follow-up
    det = api("GET", f"/api/interview/sessions/{sid}", token=cand_a_token)
    status = (det.get("data") or {}).get("status")
    if status == "IN_PROGRESS":
        for _ in range(10):
            det = api("GET", f"/api/interview/sessions/{sid}", token=cand_a_token)
            answers = (det.get("data") or {}).get("answers") or (det.get("data") or {}).get(
                "questions"
            ) or []
            cur = (det.get("data") or {}).get("currentIndex") or 0
            # find current answer entity
            cur_ans = None
            for a in answers:
                if a.get("questionIndex") == cur:
                    cur_ans = a
                    break
            if cur_ans and not cur_ans.get("answerText"):
                api(
                    "POST",
                    f"/api/interview/sessions/{sid}/answers",
                    token=cand_a_token,
                    body={"answerText": f"补答第{cur}题"},
                )
            n = api("POST", f"/api/interview/sessions/{sid}/next", token=cand_a_token)
            if not n.get("success"):
                break

    complete = api("POST", f"/api/interview/sessions/{sid}/complete", token=cand_a_token)
    rec("6.5.6_complete", code=complete["code"], message=complete["message"], data=complete.get("data"))

    # ---------- 6.6 bank/llm paths ----------
    # job1 all BANK
    s_bank = api("POST", "/api/interview/sessions", token=cand_a_token, body={"jobId": 1})
    sid_bank = (s_bank.get("data") or {}).get("id")
    d_bank = api("GET", f"/api/interview/sessions/{sid_bank}", token=cand_a_token)
    sources_bank = []
    for a in (d_bank.get("data") or {}).get("answers") or (d_bank.get("data") or {}).get(
        "questions"
    ) or []:
        sources_bank.append(a.get("source"))
    rec(
        "6.6.1_full_bank",
        code=s_bank["code"],
        message=s_bank["message"],
        sources=sources_bank,
        all_bank=bool(sources_bank) and all(s == "BANK" for s in sources_bank),
    )

    # job2 mixed
    s_mix = api("POST", "/api/interview/sessions", token=cand_a_token, body={"jobId": 2})
    sid_mix = (s_mix.get("data") or {}).get("id")
    d_mix = api("GET", f"/api/interview/sessions/{sid_mix}", token=cand_a_token)
    sources_mix = []
    for a in (d_mix.get("data") or {}).get("answers") or (d_mix.get("data") or {}).get(
        "questions"
    ) or []:
        sources_mix.append(a.get("source"))
    rec(
        "6.6.2_mixed",
        code=s_mix["code"],
        message=s_mix["message"],
        sources=sources_mix,
        has_bank="BANK" in sources_mix,
        has_llm="LLM" in sources_mix,
    )

    # zero bank: use test job with only disabled question -> set all off already, reopen job
    api("PATCH", f"/api/admin/jobs/{job_id}/status", token=admin_token, body={"status": 1})
    s_llm = api("POST", "/api/interview/sessions", token=cand_a_token, body={"jobId": job_id})
    sid_llm = (s_llm.get("data") or {}).get("id")
    d_llm = api("GET", f"/api/interview/sessions/{sid_llm}", token=cand_a_token)
    sources_llm = []
    for a in (d_llm.get("data") or {}).get("answers") or (d_llm.get("data") or {}).get(
        "questions"
    ) or []:
        sources_llm.append(a.get("source"))
    rec(
        "6.6.3_all_llm",
        code=s_llm["code"],
        message=s_llm["message"],
        sources=sources_llm,
        all_llm=bool(sources_llm) and all(s == "LLM" for s in sources_llm),
        raw_err=None if s_llm.get("success") else s_llm.get("raw"),
    )
    rec("6.6.4_llm_fail", skipped=True, note="未执行（可选负例）")

    # ---------- 6.7 eval consistency ----------
    # poll completed session sid
    eval_status = None
    user_detail = None
    for i in range(40):
        time.sleep(3)
        user_detail = api("GET", f"/api/interview/sessions/{sid}", token=cand_a_token)
        st = (user_detail.get("data") or {}).get("status")
        eval_status = st
        print(f"  poll eval {i}: status={st}", flush=True)
        if st in ("EVALUATED", "EVAL_FAILED"):
            break
    rec(
        "6.7.1_eval_done",
        status=eval_status,
        overallScore=(user_detail.get("data") or {}).get("overallScore"),
        has_feedback=bool((user_detail.get("data") or {}).get("overallFeedback")),
        data_keys=list((user_detail.get("data") or {}).keys()),
    )

    admin_detail = api(
        "GET",
        f"/api/admin/interview-records/{sid}",
        token=admin_token,
    )
    ud = user_detail.get("data") or {}
    ad = admin_detail.get("data") or {}
    score_u = ud.get("overallScore")
    score_a = ad.get("overallScore")
    comment_u = ud.get("overallFeedback")
    comment_a = ad.get("overallFeedback")
    rec(
        "6.7.2_user_detail",
        code=user_detail["code"],
        status=ud.get("status"),
        overallScore=score_u,
        has_strengths=bool(ud.get("strengths")),
        has_improvements=bool(ud.get("improvements")),
        sources=[a.get("source") for a in questions_of(ud)],
    )
    rec(
        "6.7.3_admin_detail",
        code=admin_detail["code"],
        message=admin_detail["message"],
        overallScore=score_a,
        score_match=(score_u == score_a),
        comment_match=(comment_u == comment_a) if (comment_u or comment_a) else None,
        candidateName=ad.get("candidateName") or ad.get("realName"),
        jobName=ad.get("jobName"),
    )
    rec("6.7.4_eval_fail", skipped=True, note="未执行（可选负例）")
    rec("6.8_async_sync", skipped=True, note="未执行（可选实验）")


if __name__ == "__main__":
    main()
