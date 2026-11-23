// Repaints a textured model in a team colour: keeps the texture's light and shade (its luminance)
// and takes the hue from `team`, so any model reads as the colour picked, whatever its own colours.
shader_type spatial;
render_mode diffuse_toon, specular_toon;
uniform sampler2D albedo_texture : source_color, filter_linear_mipmap;
uniform vec4 team : source_color;
// How much of the texture's own colour shows through (0 = pure team colour).
uniform float keep = 0.12;
void fragment() {
	vec3 own = texture(albedo_texture, UV).rgb;
	float light = dot(own, vec3(0.299, 0.587, 0.114));
	vec3 painted = team.rgb * (0.45 + light * 1.1);
	ALBEDO = mix(painted, own, keep);
	ROUGHNESS = 0.7;
	RIM = 0.35;
	RIM_TINT = 0.5;
}
