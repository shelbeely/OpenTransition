import 'package:flutter/material.dart';

class ResponsiveLayout extends StatelessWidget {
  final Widget mobile;
  final Widget? tablet;
  final Widget? desktop;

  const ResponsiveLayout({
    super.key,
    required this.mobile,
    this.tablet,
    this.desktop,
  });

  static bool isMobile(BuildContext context) =>
      MediaQuery.of(context).size.width < 650;

  static bool isTablet(BuildContext context) =>
      MediaQuery.of(context).size.width >= 650 &&
      MediaQuery.of(context).size.width < 1100;

  static bool isDesktop(BuildContext context) =>
      MediaQuery.of(context).size.width >= 1100;

  @override
  Widget build(BuildContext context) {
    final width = MediaQuery.of(context).size.width;

    if (width >= 1100 && desktop != null) {
      return desktop!;
    } else if (width >= 650 && tablet != null) {
      return tablet!;
    } else {
      return mobile;
    }
  }
}

class ResponsiveGridView extends StatelessWidget {
  final int mobileColumns;
  final int tabletColumns;
  final int desktopColumns;
  final Widget Function(BuildContext, int) itemBuilder;
  final int itemCount;
  final double spacing;
  final double childAspectRatio;

  const ResponsiveGridView({
    super.key,
    this.mobileColumns = 2,
    this.tabletColumns = 3,
    this.desktopColumns = 4,
    required this.itemBuilder,
    required this.itemCount,
    this.spacing = 8.0,
    this.childAspectRatio = 1.0,
  });

  int _getColumnCount(BuildContext context) {
    if (ResponsiveLayout.isDesktop(context)) {
      return desktopColumns;
    } else if (ResponsiveLayout.isTablet(context)) {
      return tabletColumns;
    } else {
      return mobileColumns;
    }
  }

  @override
  Widget build(BuildContext context) {
    return GridView.builder(
      padding: EdgeInsets.all(spacing),
      gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: _getColumnCount(context),
        crossAxisSpacing: spacing,
        mainAxisSpacing: spacing,
        childAspectRatio: childAspectRatio,
      ),
      itemCount: itemCount,
      itemBuilder: itemBuilder,
    );
  }
}

class ResponsivePadding extends StatelessWidget {
  final Widget child;
  final double mobilePadding;
  final double tabletPadding;
  final double desktopPadding;

  const ResponsivePadding({
    super.key,
    required this.child,
    this.mobilePadding = 16.0,
    this.tabletPadding = 24.0,
    this.desktopPadding = 32.0,
  });

  @override
  Widget build(BuildContext context) {
    double padding;
    if (ResponsiveLayout.isDesktop(context)) {
      padding = desktopPadding;
    } else if (ResponsiveLayout.isTablet(context)) {
      padding = tabletPadding;
    } else {
      padding = mobilePadding;
    }

    return Padding(
      padding: EdgeInsets.all(padding),
      child: child,
    );
  }
}

class ResponsiveConstraints extends StatelessWidget {
  final Widget child;
  final double maxWidth;

  const ResponsiveConstraints({
    super.key,
    required this.child,
    this.maxWidth = 1200,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: ConstrainedBox(
        constraints: BoxConstraints(maxWidth: maxWidth),
        child: child,
      ),
    );
  }
}
