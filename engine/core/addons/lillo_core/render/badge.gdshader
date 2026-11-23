// A rounded pill on a 2:0.95 quad that always faces the camera, for counters over a crowd.
shader_type spatial;
render_mode unshaded, depth_test_disabled, cull_disabled;
uniform vec4 colour : source_color;
void vertex() {
	MODELVIEW_MATRIX = VIEW_MATRIX * mat4(INV_VIEW_MATRIX[0], INV_VIEW_MATRIX[1], INV_VIEW_MATRIX[2], MODEL_MATRIX[3]);
}
void fragment() {
	vec2 p = (UV - 0.5) * vec2(2.0, 0.95);
	vec2 q = abs(p) - vec2(1.0 - 0.475, 0.0);
	float d = length(max(q, 0.0)) - 0.475;
	ALBEDO = colour.rgb;
	ALPHA = colour.a * (1.0 - smoothstep(-0.02, 0.0, d));
}
