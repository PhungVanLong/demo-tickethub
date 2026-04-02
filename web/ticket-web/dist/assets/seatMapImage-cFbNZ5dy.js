const A=/\[SEATMAP_IMAGE\]([\s\S]*?)\[\/SEATMAP_IMAGE\]/i;function M(t){var e;if(!t||typeof t!="string")return"";const r=t.match(A);return((e=r==null?void 0:r[1])==null?void 0:e.trim())||""}function a(t){return!t||typeof t!="string"?"":t.replace(A,"").trim()}function E(t,r){const e=a(t||""),n=(r||"").trim();return n?e?`${e}

[SEATMAP_IMAGE]${n}[/SEATMAP_IMAGE]`:`[SEATMAP_IMAGE]${n}[/SEATMAP_IMAGE]`:e}export{E as a,M as e,a as s};
