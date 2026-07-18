# Polypheides Resolution Scaler

A Fabric mod for Minecraft 26.3-snapshot-4 that adds a fully dynamic Render Scaling slider to the video settings menu. 

## Features
- **Dynamic Render Scaling**: Allows you to adjust the 3D rendering resolution independently of the window size, upscaling up to 200% (Supersampling) or downscaling down to 10% (for potential performance gains on GPU-bound systems).
- **Cross-Backend Compatibility**: Works flawlessly with both the OpenGL and Vulkan (`RenderPearl`) rendering backends.
- **On-the-Fly Adjustments**: Change the scale factor in-game without needing to restart.

## Installation
1. Install [Fabric Loader](https://fabricmc.net/) for Minecraft 26.3-snapshot-4.
2. Drop the `resolutionscaler-fabric-1.0.0.jar` into your `mods` folder.

## Technical Details
This mod intercepts the core frame buffer creation (`RenderTargetMixin`) and blit presentation (`GlCommandEncoderMixin` & `VulkanGpuSurfaceMixin`) to resize the 3D viewport while maintaining the original swapchain dimensions for GUI overlays. Additionally, it hooks into the `FrontendRenderPass` to align occlusion culling and scissor tests perfectly.
