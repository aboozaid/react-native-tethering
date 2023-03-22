import React from 'react';
import clsx from 'clsx';
import styles from './styles.module.css';

const FeatureList = [
  {
    title: 'Always up-to-date',
    Svg: require('@site/static/img/like.svg').default,
    description: (
      <>
        Tethering provides up-to-date connectivity functionalities behind of
        Android breaking changes.
      </>
    ),
  },
  {
    title: 'Expo supported',
    Svg: require('@site/static/img/expo.svg').default,
    description: (
      <>
        Tethering aims to support both communities react-native and expo.
      </>
    ),
  },
  // {
  //   title: 'Powered by React',
  //   Svg: require('@site/static/img/undraw_docusaurus_react.svg').default,
  //   description: (
  //     <>
  //       Extend or customize your website layout by reusing React. Docusaurus can
  //       be extended while reusing the same header and footer.
  //     </>
  //   ),
  // },
];

function Feature({Svg, title, description}) {
  return (
    <div className={clsx('col col--6')}>
      <Svg className={styles.featureSvg} role="img" />
      <div >
        <h1>{title}</h1>
        <p style={{ fontSize: '1.25rem', opacity: 0.8 }}>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures() {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
