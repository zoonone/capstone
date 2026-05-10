from __future__ import annotations

import datetime as dt
import os
import zipfile
from pathlib import Path
from xml.etree.ElementTree import Element, SubElement, tostring, register_namespace


P_NS = "http://schemas.openxmlformats.org/presentationml/2006/main"
A_NS = "http://schemas.openxmlformats.org/drawingml/2006/main"
R_NS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"
REL_NS = "http://schemas.openxmlformats.org/package/2006/relationships"
CP_NS = "http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
DC_NS = "http://purl.org/dc/elements/1.1/"
DCTERMS_NS = "http://purl.org/dc/terms/"
XSI_NS = "http://www.w3.org/2001/XMLSchema-instance"
VT_NS = "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes"

register_namespace("a", A_NS)
register_namespace("p", P_NS)
register_namespace("r", R_NS)
register_namespace("", REL_NS)
register_namespace("cp", CP_NS)
register_namespace("dc", DC_NS)
register_namespace("dcterms", DCTERMS_NS)
register_namespace("xsi", XSI_NS)
register_namespace("vt", VT_NS)


def qn(ns: str, tag: str) -> str:
    return f"{{{ns}}}{tag}"


def xml_bytes(elem: Element) -> bytes:
    return b'<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' + tostring(elem, encoding="utf-8")


def rels_xml(relationships: list[tuple[str, str, str]]) -> bytes:
    root = Element(qn(REL_NS, "Relationships"))
    for rel_id, rel_type, target in relationships:
        rel = SubElement(root, qn(REL_NS, "Relationship"))
        rel.set("Id", rel_id)
        rel.set("Type", rel_type)
        rel.set("Target", target)
    return xml_bytes(root)


def content_types_xml(slide_count: int, media_files: list[str]) -> bytes:
    ns = "http://schemas.openxmlformats.org/package/2006/content-types"
    register_namespace("", ns)
    root = Element(qn(ns, "Types"))

    defaults = {
        "rels": "application/vnd.openxmlformats-package.relationships+xml",
        "xml": "application/xml",
        "svg": "image/svg+xml",
        "png": "image/png",
    }
    for ext, content_type in defaults.items():
        default = SubElement(root, qn(ns, "Default"))
        default.set("Extension", ext)
        default.set("ContentType", content_type)

    overrides = {
        "/ppt/presentation.xml": "application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml",
        "/ppt/theme/theme1.xml": "application/vnd.openxmlformats-officedocument.theme+xml",
        "/ppt/slideMasters/slideMaster1.xml": "application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml",
        "/ppt/slideLayouts/slideLayout1.xml": "application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml",
        "/docProps/core.xml": "application/vnd.openxmlformats-package.core-properties+xml",
        "/docProps/app.xml": "application/vnd.openxmlformats-officedocument.extended-properties+xml",
    }
    for index in range(1, slide_count + 1):
        overrides[f"/ppt/slides/slide{index}.xml"] = "application/vnd.openxmlformats-officedocument.presentationml.slide+xml"

    for part_name, content_type in overrides.items():
        override = SubElement(root, qn(ns, "Override"))
        override.set("PartName", part_name)
        override.set("ContentType", content_type)
    return xml_bytes(root)


def app_xml(slide_count: int) -> bytes:
    ns = "http://schemas.openxmlformats.org/officeDocument/2006/extended-properties"
    register_namespace("", ns)
    root = Element(qn(ns, "Properties"))
    SubElement(root, qn(ns, "Application")).text = "Codex PPT Generator"
    SubElement(root, qn(ns, "PresentationFormat")).text = "On-screen Show (16:9)"
    SubElement(root, qn(ns, "Slides")).text = str(slide_count)
    SubElement(root, qn(ns, "Notes")).text = "0"
    SubElement(root, qn(ns, "HiddenSlides")).text = "0"
    SubElement(root, qn(ns, "MMClips")).text = "0"
    SubElement(root, qn(ns, "ScaleCrop")).text = "false"
    heading_pairs = SubElement(root, qn(ns, "HeadingPairs"))
    vector = SubElement(heading_pairs, qn(VT_NS, "vector"))
    vector.set("size", "2")
    vector.set("baseType", "variant")
    variant1 = SubElement(vector, qn(VT_NS, "variant"))
    SubElement(variant1, qn(VT_NS, "lpstr")).text = "Slides"
    variant2 = SubElement(vector, qn(VT_NS, "variant"))
    SubElement(variant2, qn(VT_NS, "i4")).text = str(slide_count)
    titles = SubElement(root, qn(ns, "TitlesOfParts"))
    titles_vector = SubElement(titles, qn(VT_NS, "vector"))
    titles_vector.set("size", str(slide_count))
    titles_vector.set("baseType", "lpstr")
    for index in range(1, slide_count + 1):
        SubElement(titles_vector, qn(VT_NS, "lpstr")).text = f"Slide {index}"
    SubElement(root, qn(ns, "Company")).text = "OpenAI Codex"
    SubElement(root, qn(ns, "LinksUpToDate")).text = "false"
    SubElement(root, qn(ns, "SharedDoc")).text = "false"
    SubElement(root, qn(ns, "HyperlinksChanged")).text = "false"
    SubElement(root, qn(ns, "AppVersion")).text = "1.0"
    return xml_bytes(root)


def core_xml() -> bytes:
    root = Element(qn(CP_NS, "coreProperties"))
    SubElement(root, qn(DC_NS, "title")).text = "Spring Boot 인증 서버 발표"
    SubElement(root, qn(DC_NS, "creator")).text = "OpenAI Codex"
    SubElement(root, qn(CP_NS, "lastModifiedBy")).text = "OpenAI Codex"
    now = dt.datetime.now(dt.timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z")
    created = SubElement(root, qn(DCTERMS_NS, "created"))
    created.set(qn(XSI_NS, "type"), "dcterms:W3CDTF")
    created.text = now
    modified = SubElement(root, qn(DCTERMS_NS, "modified"))
    modified.set(qn(XSI_NS, "type"), "dcterms:W3CDTF")
    modified.text = now
    return xml_bytes(root)


def theme_xml() -> bytes:
    return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<a:theme xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" name="Codex Theme">
  <a:themeElements>
    <a:clrScheme name="Codex Colors">
      <a:dk1><a:srgbClr val="102A43"/></a:dk1>
      <a:lt1><a:srgbClr val="FFFFFF"/></a:lt1>
      <a:dk2><a:srgbClr val="243B53"/></a:dk2>
      <a:lt2><a:srgbClr val="F8FBFF"/></a:lt2>
      <a:accent1><a:srgbClr val="0F4C81"/></a:accent1>
      <a:accent2><a:srgbClr val="4C956C"/></a:accent2>
      <a:accent3><a:srgbClr val="D9822B"/></a:accent3>
      <a:accent4><a:srgbClr val="6366F1"/></a:accent4>
      <a:accent5><a:srgbClr val="0284C7"/></a:accent5>
      <a:accent6><a:srgbClr val="DC2626"/></a:accent6>
      <a:hlink><a:srgbClr val="0563C1"/></a:hlink>
      <a:folHlink><a:srgbClr val="954F72"/></a:folHlink>
    </a:clrScheme>
    <a:fontScheme name="Codex Fonts">
      <a:majorFont>
        <a:latin typeface="Aptos Display"/>
        <a:ea typeface="Malgun Gothic"/>
        <a:cs typeface="Arial"/>
      </a:majorFont>
      <a:minorFont>
        <a:latin typeface="Aptos"/>
        <a:ea typeface="Malgun Gothic"/>
        <a:cs typeface="Arial"/>
      </a:minorFont>
    </a:fontScheme>
    <a:fmtScheme name="Codex Format">
      <a:fillStyleLst>
        <a:solidFill><a:schemeClr val="lt1"/></a:solidFill>
        <a:solidFill><a:schemeClr val="accent1"/></a:solidFill>
        <a:solidFill><a:schemeClr val="accent2"/></a:solidFill>
      </a:fillStyleLst>
      <a:lnStyleLst>
        <a:ln w="9525"><a:solidFill><a:schemeClr val="accent1"/></a:solidFill></a:ln>
        <a:ln w="25400"><a:solidFill><a:schemeClr val="accent2"/></a:solidFill></a:ln>
        <a:ln w="38100"><a:solidFill><a:schemeClr val="accent3"/></a:solidFill></a:ln>
      </a:lnStyleLst>
      <a:effectStyleLst><a:effectStyle/></a:effectStyleLst>
      <a:bgFillStyleLst>
        <a:solidFill><a:schemeClr val="lt2"/></a:solidFill>
        <a:solidFill><a:schemeClr val="lt1"/></a:solidFill>
      </a:bgFillStyleLst>
    </a:fmtScheme>
  </a:themeElements>
  <a:objectDefaults/>
  <a:extraClrSchemeLst/>
</a:theme>
""".encode("utf-8")


def slide_master_xml() -> bytes:
    return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldMaster xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
             xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"
             xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <p:cSld name="Master">
    <p:bg>
      <p:bgPr>
        <a:solidFill><a:srgbClr val="F8FBFF"/></a:solidFill>
      </p:bgPr>
    </p:bg>
    <p:spTree>
      <p:nvGrpSpPr>
        <p:cNvPr id="1" name=""/>
        <p:cNvGrpSpPr/>
        <p:nvPr/>
      </p:nvGrpSpPr>
      <p:grpSpPr>
        <a:xfrm>
          <a:off x="0" y="0"/>
          <a:ext cx="0" cy="0"/>
          <a:chOff x="0" y="0"/>
          <a:chExt cx="0" cy="0"/>
        </a:xfrm>
      </p:grpSpPr>
    </p:spTree>
  </p:cSld>
  <p:clrMap accent1="accent1" accent2="accent2" accent3="accent3" accent4="accent4" accent5="accent5" accent6="accent6" bg1="lt1" bg2="lt2" folHlink="folHlink" hlink="hlink" tx1="dk1" tx2="dk2"/>
  <p:sldLayoutIdLst>
    <p:sldLayoutId id="2147483649" r:id="rId1"/>
  </p:sldLayoutIdLst>
  <p:txStyles>
    <p:titleStyle/>
    <p:bodyStyle/>
    <p:otherStyle/>
  </p:txStyles>
</p:sldMaster>
""".encode("utf-8")


def slide_layout_xml() -> bytes:
    return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldLayout xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
             xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"
             xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
             type="blank" preserve="1">
  <p:cSld name="Blank">
    <p:spTree>
      <p:nvGrpSpPr>
        <p:cNvPr id="1" name=""/>
        <p:cNvGrpSpPr/>
        <p:nvPr/>
      </p:nvGrpSpPr>
      <p:grpSpPr>
        <a:xfrm>
          <a:off x="0" y="0"/>
          <a:ext cx="0" cy="0"/>
          <a:chOff x="0" y="0"/>
          <a:chExt cx="0" cy="0"/>
        </a:xfrm>
      </p:grpSpPr>
    </p:spTree>
  </p:cSld>
  <p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>
</p:sldLayout>
""".encode("utf-8")


def presentation_xml(slide_count: int) -> bytes:
    root = Element(qn(P_NS, "presentation"))
    root.set("saveSubsetFonts", "1")
    sld_master_id_lst = SubElement(root, qn(P_NS, "sldMasterIdLst"))
    master = SubElement(sld_master_id_lst, qn(P_NS, "sldMasterId"))
    master.set("id", "2147483648")
    master.set(qn(R_NS, "id"), "rId1")

    sld_id_lst = SubElement(root, qn(P_NS, "sldIdLst"))
    for index in range(1, slide_count + 1):
        slide = SubElement(sld_id_lst, qn(P_NS, "sldId"))
        slide.set("id", str(255 + index))
        slide.set(qn(R_NS, "id"), f"rId{index + 1}")

    slide_size = SubElement(root, qn(P_NS, "sldSz"))
    slide_size.set("cx", "12192000")
    slide_size.set("cy", "6858000")
    notes_size = SubElement(root, qn(P_NS, "notesSz"))
    notes_size.set("cx", "6858000")
    notes_size.set("cy", "9144000")
    return xml_bytes(root)


def add_text_paragraph(tx_body: Element, text: str, font_size: int, color: str, bold: bool = False) -> None:
    paragraph = SubElement(tx_body, qn(A_NS, "p"))
    run = SubElement(paragraph, qn(A_NS, "r"))
    run_props = SubElement(run, qn(A_NS, "rPr"))
    run_props.set("lang", "ko-KR")
    run_props.set("sz", str(font_size * 100))
    run_props.set("dirty", "0")
    run_props.set("smtClean", "0")
    if bold:
        run_props.set("b", "1")
    solid = SubElement(run_props, qn(A_NS, "solidFill"))
    SubElement(solid, qn(A_NS, "srgbClr")).set("val", color)
    SubElement(run, qn(A_NS, "t")).text = text
    end_props = SubElement(paragraph, qn(A_NS, "endParaRPr"))
    end_props.set("lang", "ko-KR")
    end_props.set("sz", str(font_size * 100))


def add_textbox(sp_tree: Element, shape_id: int, name: str, x: int, y: int, cx: int, cy: int,
                paragraphs: list[tuple[str, int, str, bool]]) -> int:
    sp = SubElement(sp_tree, qn(P_NS, "sp"))
    nv_sp_pr = SubElement(sp, qn(P_NS, "nvSpPr"))
    c_nv_pr = SubElement(nv_sp_pr, qn(P_NS, "cNvPr"))
    c_nv_pr.set("id", str(shape_id))
    c_nv_pr.set("name", name)
    SubElement(nv_sp_pr, qn(P_NS, "cNvSpPr"))
    SubElement(nv_sp_pr, qn(P_NS, "nvPr"))
    sp_pr = SubElement(sp, qn(P_NS, "spPr"))
    xfrm = SubElement(sp_pr, qn(A_NS, "xfrm"))
    off = SubElement(xfrm, qn(A_NS, "off"))
    off.set("x", str(x))
    off.set("y", str(y))
    ext = SubElement(xfrm, qn(A_NS, "ext"))
    ext.set("cx", str(cx))
    ext.set("cy", str(cy))
    prst = SubElement(sp_pr, qn(A_NS, "prstGeom"))
    prst.set("prst", "rect")
    SubElement(prst, qn(A_NS, "avLst"))
    tx_body = SubElement(sp, qn(P_NS, "txBody"))
    body_pr = SubElement(tx_body, qn(A_NS, "bodyPr"))
    body_pr.set("wrap", "square")
    SubElement(tx_body, qn(A_NS, "lstStyle"))
    for paragraph in paragraphs:
        add_text_paragraph(tx_body, *paragraph)
    return shape_id + 1


def add_banner(sp_tree: Element, shape_id: int) -> int:
    sp = SubElement(sp_tree, qn(P_NS, "sp"))
    nv_sp_pr = SubElement(sp, qn(P_NS, "nvSpPr"))
    c_nv_pr = SubElement(nv_sp_pr, qn(P_NS, "cNvPr"))
    c_nv_pr.set("id", str(shape_id))
    c_nv_pr.set("name", "Top Banner")
    SubElement(nv_sp_pr, qn(P_NS, "cNvSpPr"))
    SubElement(nv_sp_pr, qn(P_NS, "nvPr"))
    sp_pr = SubElement(sp, qn(P_NS, "spPr"))
    xfrm = SubElement(sp_pr, qn(A_NS, "xfrm"))
    off = SubElement(xfrm, qn(A_NS, "off"))
    off.set("x", "0")
    off.set("y", "0")
    ext = SubElement(xfrm, qn(A_NS, "ext"))
    ext.set("cx", "12192000")
    ext.set("cy", "685800")
    prst = SubElement(sp_pr, qn(A_NS, "prstGeom"))
    prst.set("prst", "rect")
    SubElement(prst, qn(A_NS, "avLst"))
    fill = SubElement(sp_pr, qn(A_NS, "solidFill"))
    SubElement(fill, qn(A_NS, "srgbClr")).set("val", "0F4C81")
    ln = SubElement(sp_pr, qn(A_NS, "ln"))
    ln.set("w", "0")
    return shape_id + 1


def add_image(sp_tree: Element, shape_id: int, rel_id: str, x: int, y: int, cx: int, cy: int) -> int:
    pic = SubElement(sp_tree, qn(P_NS, "pic"))
    nv_pic_pr = SubElement(pic, qn(P_NS, "nvPicPr"))
    c_nv_pr = SubElement(nv_pic_pr, qn(P_NS, "cNvPr"))
    c_nv_pr.set("id", str(shape_id))
    c_nv_pr.set("name", f"Picture {shape_id}")
    SubElement(nv_pic_pr, qn(P_NS, "cNvPicPr"))
    SubElement(nv_pic_pr, qn(P_NS, "nvPr"))
    blip_fill = SubElement(pic, qn(P_NS, "blipFill"))
    blip = SubElement(blip_fill, qn(A_NS, "blip"))
    blip.set(qn(R_NS, "embed"), rel_id)
    stretch = SubElement(blip_fill, qn(A_NS, "stretch"))
    SubElement(stretch, qn(A_NS, "fillRect"))
    sp_pr = SubElement(pic, qn(P_NS, "spPr"))
    xfrm = SubElement(sp_pr, qn(A_NS, "xfrm"))
    off = SubElement(xfrm, qn(A_NS, "off"))
    off.set("x", str(x))
    off.set("y", str(y))
    ext = SubElement(xfrm, qn(A_NS, "ext"))
    ext.set("cx", str(cx))
    ext.set("cy", str(cy))
    prst = SubElement(sp_pr, qn(A_NS, "prstGeom"))
    prst.set("prst", "rect")
    SubElement(prst, qn(A_NS, "avLst"))
    return shape_id + 1


def slide_xml(title: str, bullets: list[str], image_rel_id: str | None = None) -> bytes:
    root = Element(qn(P_NS, "sld"))
    csld = SubElement(root, qn(P_NS, "cSld"))
    sp_tree = SubElement(csld, qn(P_NS, "spTree"))
    nv_grp = SubElement(sp_tree, qn(P_NS, "nvGrpSpPr"))
    c_nv_pr = SubElement(nv_grp, qn(P_NS, "cNvPr"))
    c_nv_pr.set("id", "1")
    c_nv_pr.set("name", "")
    SubElement(nv_grp, qn(P_NS, "cNvGrpSpPr"))
    SubElement(nv_grp, qn(P_NS, "nvPr"))
    grp_sp_pr = SubElement(sp_tree, qn(P_NS, "grpSpPr"))
    xfrm = SubElement(grp_sp_pr, qn(A_NS, "xfrm"))
    off = SubElement(xfrm, qn(A_NS, "off"))
    off.set("x", "0")
    off.set("y", "0")
    ext = SubElement(xfrm, qn(A_NS, "ext"))
    ext.set("cx", "0")
    ext.set("cy", "0")
    ch_off = SubElement(xfrm, qn(A_NS, "chOff"))
    ch_off.set("x", "0")
    ch_off.set("y", "0")
    ch_ext = SubElement(xfrm, qn(A_NS, "chExt"))
    ch_ext.set("cx", "0")
    ch_ext.set("cy", "0")

    shape_id = 2
    shape_id = add_banner(sp_tree, shape_id)
    shape_id = add_textbox(
        sp_tree,
        shape_id,
        "Title",
        640080,
        420000,
        10800000,
        700000,
        [(title, 28, "102A43", True)],
    )

    text_paragraphs = []
    for bullet in bullets:
        text_paragraphs.append((f"• {bullet}", 18, "334E68", False))
    body_width = 5200000 if image_rel_id else 9800000
    shape_id = add_textbox(
        sp_tree,
        shape_id,
        "Body",
        640080,
        1180080,
        body_width,
        4500000,
        text_paragraphs,
    )

    if image_rel_id:
        shape_id = add_image(sp_tree, shape_id, image_rel_id, 6600000, 1400000, 4900000, 3400000)

    clr_map = SubElement(root, qn(P_NS, "clrMapOvr"))
    SubElement(clr_map, qn(A_NS, "masterClrMapping"))
    return xml_bytes(root)


def slide_rels_xml(layout_rel: str = "../slideLayouts/slideLayout1.xml", image_target: str | None = None) -> bytes:
    relationships = [
        (
            "rId1",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout",
            layout_rel,
        )
    ]
    if image_target:
        relationships.append(
            (
                "rId2",
                "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
                image_target,
            )
        )
    return rels_xml(relationships)


SLIDES = [
    {
        "title": "Spring Boot 인증 서버 발표",
        "bullets": [
            "주제: JWT + OAuth2 기반 인증 백엔드 설계",
            "이번 자료는 구조 설명과 함께 실제 동작 코드를 같이 보여줍니다.",
            "핵심 메시지: 왜 이렇게 나눴는지와 요청이 어떻게 흐르는지를 설명합니다.",
        ],
        "image": None,
    },
    {
        "title": "프로젝트 개요",
        "bullets": [
            "로컬 회원가입과 로그인 지원",
            "JWT access token 발급과 인증 유지",
            "Google, Kakao, Naver OAuth2 로그인 지원",
            "전역 예외 처리와 요청 검증, 테스트 코드까지 포함",
        ],
        "image": None,
    },
    {
        "title": "패키지 구조",
        "bullets": [
            "controller는 요청과 응답 형식을 담당합니다.",
            "service는 회원가입과 로그인 로직을 담당합니다.",
            "repository는 사용자 조회와 저장 같은 DB 접근을 맡습니다.",
            "security와 global 패키지는 인증, 예외 처리 같은 공통 로직을 모았습니다.",
        ],
        "image": "package-structure.svg",
    },
    {
        "title": "시스템 구조",
        "bullets": [
            "기본 로그인 요청은 AuthController -> AuthService -> UserRepository 순서로 흐릅니다.",
            "SecurityConfig가 필터 체인, CORS, OAuth2 로그인 설정을 통합합니다.",
            "JwtFilter와 JwtUtil이 토큰 검증과 인증 객체 주입을 담당합니다.",
        ],
        "image": "system-architecture.svg",
    },
    {
        "title": "실제 코드: AuthController",
        "bullets": [
            "@Valid와 DTO를 이용해 컨트롤러에서 1차 검증을 합니다.",
            "컨트롤러는 서비스 호출 후 HTTP 상태 코드와 응답 형태를 결정합니다.",
            "즉, 컨트롤러는 얇게 두고 핵심 로직은 서비스에 넘기는 구조입니다.",
        ],
        "image": "auth-controller-code.svg",
    },
    {
        "title": "실제 코드: AuthService",
        "bullets": [
            "signup()은 중복 이메일을 확인하고 비밀번호를 암호화해서 저장합니다.",
            "login()은 사용자 조회, provider 검사, 비밀번호 비교를 거칩니다.",
            "로그인 성공 시 JwtUtil로 JWT를 생성해 반환합니다.",
        ],
        "image": "auth-service-code.svg",
    },
    {
        "title": "로컬 로그인 처리 흐름",
        "bullets": [
            "클라이언트가 /api/auth/login 요청을 보냅니다.",
            "서비스 계층에서 사용자 조회와 provider 검사를 수행합니다.",
            "BCrypt로 비밀번호 해시를 비교한 뒤 JwtUtil로 토큰을 생성합니다.",
            "세션 대신 토큰을 반환해 API 서버 구조를 단순하게 유지합니다.",
        ],
        "image": "local-login-flow.svg",
    },
    {
        "title": "실제 코드: JwtFilter",
        "bullets": [
            "Authorization 헤더에서 Bearer 토큰을 추출합니다.",
            "토큰이 유효하면 이메일을 꺼내 Authentication 객체를 만듭니다.",
            "이 인증 객체를 SecurityContext에 넣어서 이후 컨트롤러가 인증된 사용자로 인식하게 합니다.",
        ],
        "image": "jwt-filter-code.svg",
    },
    {
        "title": "OAuth2 로그인 처리 흐름",
        "bullets": [
            "Spring Security가 소셜 로그인 흐름을 시작합니다.",
            "CustomOAuth2UserService가 provider별 사용자 정보를 공통 형식으로 변환합니다.",
            "기존 회원은 갱신하고 신규 회원은 생성하는 upsert 전략을 사용합니다.",
            "성공 시 JWT를 발급해 프론트 콜백 URL로 리다이렉트합니다.",
        ],
        "image": "oauth2-flow.svg",
    },
    {
        "title": "실제 코드: OAuth2 사용자 처리",
        "bullets": [
            "OAuth2UserInfoFactory가 provider별 클래스를 선택합니다.",
            "provider마다 응답 JSON 구조가 달라도 공통 인터페이스로 맞춥니다.",
            "이 덕분에 새로운 소셜 로그인을 추가할 때 확장성이 좋아집니다.",
        ],
        "image": "oauth2-code.svg",
    },
    {
        "title": "실제 코드: 예외 처리와 검증",
        "bullets": [
            "DTO에서 email과 password를 애노테이션으로 검증합니다.",
            "GlobalExceptionHandler가 인증 관련 검증 실패를 같은 메시지로 통일합니다.",
            "왜 이렇게 했는가: 어떤 정보가 틀렸는지 노출하지 않아 보안상 유리합니다.",
        ],
        "image": "exception-code.svg",
    },
    {
        "title": "예외 처리와 테스트 전략",
        "bullets": [
            "입력 검증 실패와 비즈니스 예외를 한곳에서 처리합니다.",
            "AuthServiceTest가 로그인/회원가입 핵심 로직을 검증합니다.",
            "AuthControllerApiTest가 HTTP 상태 코드와 JSON 응답 형식을 검증합니다.",
            "JWT와 OAuth2 테스트로 보안 흐름의 회귀를 방지합니다.",
        ],
        "image": "exception-test-overview.svg",
    },
    {
        "title": "실제 코드: 테스트",
        "bullets": [
            "서비스 테스트는 JWT 생성과 provider 차단 로직을 검증합니다.",
            "컨트롤러 테스트는 상태 코드와 에러 메시지를 검증합니다.",
            "발표에서는 기능 구현뿐 아니라 검증 코드까지 준비했다는 점을 강조하면 좋습니다.",
        ],
        "image": "test-code.svg",
    },
    {
        "title": "시연 포인트와 마무리",
        "bullets": [
            "회원가입 -> 로그인 -> access token 확인 순서로 시연합니다.",
            "Authorization 헤더로 /api/auth/me 호출 결과를 보여줍니다.",
            "소셜 로그인 URL과 구조 설명으로 확장성을 강조합니다.",
            "핵심 메시지: 기능 구현보다 설명 가능한 구조 설계에 초점을 맞췄습니다.",
        ],
        "image": None,
    },
]


def ensure_parent(path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)


def build_presentation(output_path: Path) -> None:
    base_dir = Path(__file__).resolve().parent
    assets_dir = base_dir / "assets"
    ensure_parent(output_path)

    with zipfile.ZipFile(output_path, "w", compression=zipfile.ZIP_DEFLATED) as zf:
        zf.writestr("[Content_Types].xml", content_types_xml(len(SLIDES), []))
        zf.writestr("_rels/.rels", rels_xml([
            ("rId1", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument", "ppt/presentation.xml"),
            ("rId2", "http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties", "docProps/core.xml"),
            ("rId3", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties", "docProps/app.xml"),
        ]))
        zf.writestr("docProps/core.xml", core_xml())
        zf.writestr("docProps/app.xml", app_xml(len(SLIDES)))
        zf.writestr("ppt/theme/theme1.xml", theme_xml())
        zf.writestr("ppt/slideMasters/slideMaster1.xml", slide_master_xml())
        zf.writestr("ppt/slideMasters/_rels/slideMaster1.xml.rels", rels_xml([
            ("rId1", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout", "../slideLayouts/slideLayout1.xml"),
            ("rId2", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme", "../theme/theme1.xml"),
        ]))
        zf.writestr("ppt/slideLayouts/slideLayout1.xml", slide_layout_xml())
        zf.writestr("ppt/slideLayouts/_rels/slideLayout1.xml.rels", rels_xml([
            ("rId1", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster", "../slideMasters/slideMaster1.xml"),
        ]))
        zf.writestr("ppt/presentation.xml", presentation_xml(len(SLIDES)))

        presentation_relationships = [
            ("rId1", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster", "slideMasters/slideMaster1.xml")
        ]
        for index in range(1, len(SLIDES) + 1):
            presentation_relationships.append(
                (
                    f"rId{index + 1}",
                    "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide",
                    f"slides/slide{index}.xml",
                )
            )
        zf.writestr("ppt/_rels/presentation.xml.rels", rels_xml(presentation_relationships))

        media_index = 1
        for index, slide in enumerate(SLIDES, start=1):
            image_rel = None
            image_target = None
            if slide["image"]:
                image_name = slide["image"]
                image_path = assets_dir / f"{image_name}.png"
                media_ext = "png"
                if not image_path.exists():
                    image_path = assets_dir / image_name
                    media_ext = image_path.suffix.lstrip(".")
                image_rel = "rId2"
                image_target = f"../media/image{media_index}.{media_ext}"
                zf.writestr(f"ppt/media/image{media_index}.{media_ext}", image_path.read_bytes())
                media_index += 1

            zf.writestr(
                f"ppt/slides/slide{index}.xml",
                slide_xml(slide["title"], slide["bullets"], image_rel_id=image_rel),
            )
            zf.writestr(
                f"ppt/slides/_rels/slide{index}.xml.rels",
                slide_rels_xml(image_target=image_target),
            )


if __name__ == "__main__":
    build_presentation(Path(__file__).resolve().parent / "캡스톤발표.pptx")
